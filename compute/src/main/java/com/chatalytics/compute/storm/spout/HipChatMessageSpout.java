package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IChatAlyticsDAO;
import com.chatalytics.compute.hipchat.dao.HipChatApiDAOFactory;
import com.chatalytics.compute.util.YamlUtils;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.FatMessage;
import com.chatalytics.core.model.Message;
import com.chatalytics.core.model.Room;
import com.chatalytics.core.model.User;
import com.google.common.collect.Lists;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Spout that pulls messages from the hipchat API and emits {@link FatMessage}s to subscribed bolts.
 *
 * @author giannis
 */
public class HipChatMessageSpout extends BaseRichSpout {

    private static final long serialVersionUID = -1059483249301395751L;
    public static final String SPOUT_ID = "HIP_CHAT_MESSAGE_SPOUT_ID";
    public static final String HIPCHAT_MESSAGE_FIELD_STR = "hipchat-message";
    private static final Logger LOG = LoggerFactory.getLogger(HipChatMessageSpout.class);

    private IChatApiDAO hipchatDao;
    private DateTimeZone dtz;
    private SpoutOutputCollector collector;
    private IChatAlyticsDAO dbDao;

    @Override
    public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
                     SpoutOutputCollector collector) {
        String configYaml = (String) conf.get(ConfigurationConstants.CHATALYTICS_CONFIG.txt);
        ChatAlyticsConfig config = YamlUtils.readYamlFromString(configYaml,
                                                                 ChatAlyticsConfig.class);
        LOG.info("Loaded config...");

        hipchatDao = HipChatApiDAOFactory.getHipChatApiDao(config);
        LOG.info("Got HipChat API DAO...");

        dbDao = ChatAlyticsDAOFactory.createChatAlyticsDao(config);
        LOG.info("Got database DAO...");

        dtz = DateTimeZone.forID(config.timeZone);
        this.collector = collector;
    }

    @Override
    public void nextTuple() {
        DateTime newPullEndDate = truncateDateTimeToHour(DateTime.now(dtz));
        DateTime lastPullTime = truncateDateTimeToHour(dbDao.getLastMessagePullTime());
        if (lastPullTime.isEqual(newPullEndDate) || lastPullTime.isAfter(newPullEndDate)) {
            LOG.info("Not ready to pull data yet. Last pull time was {}, new pull end date was {}",
                     lastPullTime, newPullEndDate);
            return;
        }
        Map<String, Room> rooms = hipchatDao.getRooms();
        List<FatMessage> messagesToEmit = Lists.newArrayList();
        for (Room room : rooms.values()) {
            List<Message> messages = hipchatDao.getMessages(lastPullTime, newPullEndDate, room);
            Map<String, User> users = hipchatDao.getUsers();
            for (Message message : messages) {
                User user = users.get(message.getFromUserId());
                messagesToEmit.add(new FatMessage(message, user, room));
            }
        }
        for (FatMessage fatMessage : messagesToEmit) {
            collector.emit(new Values(fatMessage));
        }
        try {
            Thread.sleep(1 * 60 * 60 * 1000); // sleep for 1h.
        } catch (InterruptedException e) {
            LOG.error("Got interrupted while sleeping.", e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(HIPCHAT_MESSAGE_FIELD_STR));
    }

    private DateTime truncateDateTimeToHour(DateTime dateTime) {
        return dateTime.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }

}
