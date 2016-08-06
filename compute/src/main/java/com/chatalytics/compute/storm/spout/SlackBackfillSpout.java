package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.chat.dao.slack.SlackApiDAOFactory;
import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IChatAlyticsDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.SlackBackfillerConfig;
import com.chatalytics.core.model.data.FatMessage;
import com.chatalytics.core.model.data.Message;
import com.chatalytics.core.model.data.MessageType;
import com.chatalytics.core.model.data.Room;
import com.chatalytics.core.model.data.User;
import com.chatalytics.core.util.YamlUtils;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import org.apache.storm.shade.com.google.common.base.Preconditions;
import org.apache.storm.shade.com.google.common.collect.Sets;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Spout to be used for batching and/or back filling. Look at {@link SlackBackfillerConfig} for
 * configuration options. Note that this spout only supports {@link #MAX_BACKFILL_DAYS}
 *
 * @author giannis
 */
public class SlackBackfillSpout extends BaseRichSpout {

    private static final long serialVersionUID = 6649823322353792848L;
    private static final Logger LOG = LoggerFactory.getLogger(SlackBackfillSpout.class);

    public static final String SPOUT_ID = "SLACK_BACKFILL_MESSAGE_SPOUT_ID";
    public static final String BACKFILL_SLACK_MESSAGE_FIELD_STR = "slack-message";

    private DateTime initDate;
    private DateTime endDate;
    private SpoutOutputCollector collector;
    private int granularityMins;
    private IChatApiDAO slackDao;
    private IChatAlyticsDAO dbDao;

    @Override
    public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
                     SpoutOutputCollector collector) {

        String configYaml = (String) conf.get(ConfigurationConstants.CHATALYTICS_CONFIG.txt);
        ChatAlyticsConfig config = YamlUtils.readChatAlyticsConfigFromString(configYaml);
        SlackBackfillerConfig chatConfig = (SlackBackfillerConfig) config.computeConfig.chatConfig;
        Preconditions.checkArgument(chatConfig.granularityMins >= 0, "Granularity has to be >= 0");

        open(chatConfig, SlackApiDAOFactory.getSlackApiDao(config),
             ChatAlyticsDAOFactory.createChatAlyticsDao(config), context, collector);
    }

    @VisibleForTesting
    protected void open(SlackBackfillerConfig chatConfig, IChatApiDAO slackApiDao,
                        IChatAlyticsDAO dbDao, TopologyContext context,
                        SpoutOutputCollector collector) {
        this.granularityMins = chatConfig.granularityMins;
        this.collector = collector;
        this.slackDao = slackApiDao;
        this.dbDao = dbDao;

        // get start date
        if (chatConfig.startDate == null) {
            // go back a day
            this.initDate = new DateTime(DateTimeZone.UTC).withHourOfDay(0)
                                                          .withMinuteOfHour(0)
                                                          .minusDays(1);
        } else {
            this.initDate = DateTime.parse(chatConfig.startDate);
        }

        // get end date, if there is one
        if (chatConfig.endDate != null) {
            this.endDate = DateTime.parse(chatConfig.endDate);
        }
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
                LOG.info("Waiting for a few more minutes to go by. Granularity is {}",
                         granularityMins);
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
        backfillRooms(users, rooms, runInterval);
        dbDao.setLastMessagePullTime(runInterval.getEnd());
    }

    @VisibleForTesting
    protected void backfillRooms(Map<String, User> users, Map<String, Room> rooms,
                                 Interval runInterval) {
        Set<String> processedRoomNames = Sets.newHashSet(rooms.keySet());
        int skippedUnknownMessages = 0;
        LOG.info("Backfilling {} rooms", rooms.size());
        int roomNum = 0;
        for (Room room : rooms.values()) {
            roomNum++;
            processedRoomNames.add(room.getName());

            List<Message> messages = slackDao.getMessages(runInterval.getStart(),
                                                          runInterval.getEnd(),
                                                          room);
            for (Message message : messages) {
                User user = users.get(message.getFromUserId());
                if (message.getType() == MessageType.UNKNOWN) {
                    LOG.debug("Skipping unkown message type. {}", message);
                    skippedUnknownMessages++;
                    continue;
                } else if  (user == null && message.getType() == MessageType.BOT_MESSAGE) {
                    user = new User(message.getFromUserId(), null, false, false, true,
                                    message.getFromName(), message.getFromName(), null,
                                    DateTime.now(), null, null, null, null, null);
                }
                if (user == null) {
                    LOG.warn("Can't find user with userId: {}. Skipping", message.getFromUserId());
                    continue;
                }
                FatMessage fatMessage = new FatMessage(message, user, room);
                collector.emit(new Values(fatMessage));
            }
            logProgress(roomNum, rooms.size());

        }
        LOG.info("Finished backfilling. Skipped {} unknown msgs. Processed {} rooms. They were: {}",
                 skippedUnknownMessages, processedRoomNames.size(), processedRoomNames);
    }

    private void logProgress(int roomNum, int totalRooms) {
        if (roomNum % 20 == 0) {
            LOG.info("Successfully backfilled {}/{} rooms", roomNum, totalRooms);
        }
    }

    /**
     * Note that a call to this method may block up to a configurable amount of time if the next
     * start time is after now
     *
     * @return The next run interval to get messages for based on the last pull time, the
     *         granularity of batch gets and the initial start date set in the yaml config
     */
    @VisibleForTesting
    protected Optional<Interval> getRunInterval() {
        DateTime startDate;
        DateTime nextEndDate = new DateTime(System.currentTimeMillis(), DateTimeZone.UTC);
        if (nextEndDate.isAfter(endDate)) {
            nextEndDate = endDate;
        }
        DateTime lastRunDate = dbDao.getLastMessagePullTime();

        if (initDate.isAfter(lastRunDate)) {
            startDate = initDate;
        } else {
            startDate = lastRunDate;
        }

        if (startDate.isBefore(endDate) &&
                startDate.plusMinutes(granularityMins).isAfter(nextEndDate)) {
            return Optional.of(new Interval(startDate, endDate));
        } else if (startDate.plusMinutes(granularityMins).isAfter(nextEndDate)) {
            return Optional.absent();
        } else {
            return Optional.of(new Interval(startDate, nextEndDate));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(BACKFILL_SLACK_MESSAGE_FIELD_STR));
    }

    @Override
    public void close() {
        if (dbDao != null && dbDao.isRunning()) {
            dbDao.stopAsync().awaitTerminated();
        }
    }

}
