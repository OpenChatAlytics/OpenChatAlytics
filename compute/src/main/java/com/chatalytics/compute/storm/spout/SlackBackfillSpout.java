package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IChatAlyticsDAO;
import com.chatalytics.compute.slack.dao.SlackApiDAOFactory;
import com.chatalytics.compute.util.YamlUtils;
import com.chatalytics.core.config.BackfillerConfig;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.FatMessage;
import com.chatalytics.core.model.Message;
import com.chatalytics.core.model.Room;
import com.chatalytics.core.model.User;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import org.apache.storm.shade.com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.util.List;
import java.util.Map;

/**
 * Spout to be used for batching and/or back filling. Look at {@link BackfillerConfig} for
 * configuration options. Note that this spout only supports {@link #MAX_BACKFILL_DAYS}
 *
 * @author giannis
 */
public class SlackBackfillSpout extends BaseRichSpout {

    private static final long serialVersionUID = 6649823322353792848L;
    private static final Logger LOG = LoggerFactory.getLogger(SlackBackfillSpout.class);

    public static final String SPOUT_ID = "SLACK_BACKFILL_MESSAGE_SPOUT_ID";
    public static final String BACKFILL_SLACK_MESSAGE_FIELD_STR = "slack-message";
    private static final int MAX_BACKFILL_DAYS = 7;

    private IChatApiDAO slackDao;
    private DateTime initDate;
    private SpoutOutputCollector collector;
    private int granularityMins;
    private IChatAlyticsDAO dbDao;

    @Override
    public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
                     SpoutOutputCollector collector) {

        String configYaml = (String) conf.get(ConfigurationConstants.CHATALYTICS_CONFIG.txt);
        ChatAlyticsConfig config = YamlUtils.readYamlFromString(configYaml,
                                                                ChatAlyticsConfig.class);

        BackfillerConfig backfillerConfig = config.computeConfig.backfillerConfig;
        Preconditions.checkArgument(backfillerConfig.granularityMins > 0,
                "The granularity needs to be > 0");
        this.granularityMins = backfillerConfig.granularityMins;
        this.slackDao = getChatApiDao(config);
        this.collector = collector;
        if (backfillerConfig.startDate == null) {
            // go back a day
            this.initDate = new DateTime(DateTimeZone.UTC).withHourOfDay(0)
                                                           .withMinuteOfHour(0)
                                                           .minusDays(1);
        } else {
            this.initDate = DateTime.parse(backfillerConfig.startDate);
        }

        this.dbDao = ChatAlyticsDAOFactory.getChatAlyticsDao(config);
    }

    /**
     * @return The slack API DAO
     */
    @VisibleForTesting
    protected IChatApiDAO getChatApiDao(ChatAlyticsConfig  config) {
        return SlackApiDAOFactory.getSlackApiDao(config);
    }

    /**
     * Iterates over all the rooms and for each room gets the history of the messages in the given
     * time period and builds a {@link FatMessage} and emits it
     */
    @Override
    public void nextTuple() {

        Optional<Interval> optionalInterval = getRunInterval();
        if (!optionalInterval.isPresent()) {
            try {
                Thread.sleep(granularityMins * 60 * 1000);
            } catch (InterruptedException e) {
                LOG.warn("Interrupted while sleeping...");
            }
            return;
        }
        Interval runInterval = optionalInterval.get();

        LOG.info("Running with the following interval {}", runInterval);

        Map<String, User> users = slackDao.getUsers();
        // get all the rooms and for each room get the messages
        Map<String, Room> rooms = slackDao.getRooms();
        for (Room room : rooms.values()) {

            if (room.isArchived()) {
                LOG.info("Skipping archived room {}", room.getName());
                continue;
            }

            List<Message> messages = slackDao.getMessages(runInterval.getStart(),
                                                          runInterval.getEnd(),
                                                          room);
            for (Message message : messages) {
                User user = users.get(message.getFromUserId());
                if (user == null) {
                    LOG.warn("Can't find user with userId: {}. Skipping", message.getFromUserId());
                    continue;
                }
                FatMessage fatMessage = new FatMessage(message, user, room);
                collector.emit(new Values(fatMessage));
            }
        }
        LOG.info("Finished backfilling...");
        dbDao.setLastMessagePullTime(runInterval.getEnd());
    }

    /**
     * @return The next run interval to get messages for based on the last pull time, the
     *         granularity of batch gets and the initial start date set in the yaml config
     */
    private Optional<Interval> getRunInterval() {
        DateTime startDate;
        DateTime endDate = new DateTime(System.currentTimeMillis(), DateTimeZone.UTC);
        DateTime lastRunDate = dbDao.getLastMessagePullTime();

        if (initDate.isAfter(lastRunDate)) {
            startDate = initDate;
        } else {
            startDate = lastRunDate;
        }

        if (startDate.plusMinutes(granularityMins).isAfter(endDate)) {
            LOG.info("Waiting for a few more minutes to go by. Granularity is {}", granularityMins);
            return Optional.absent();
        }

        Interval runInterval = new Interval(startDate, endDate);
        if (runInterval.toDuration().getStandardDays() > MAX_BACKFILL_DAYS) {
            startDate = endDate.minusDays(MAX_BACKFILL_DAYS);
            runInterval = new Interval(startDate, endDate);
        }

        return Optional.of(runInterval);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(BACKFILL_SLACK_MESSAGE_FIELD_STR));
    }

}
