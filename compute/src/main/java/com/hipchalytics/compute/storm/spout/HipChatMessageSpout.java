package com.hipchalytics.compute.storm.spout;

import com.google.common.collect.Lists;
import com.hipchalytics.compute.config.ConfigurationConstants;
import com.hipchalytics.compute.config.HipChalyticsConfig;
import com.hipchalytics.compute.db.dao.HipChalyticsDaoFactory;
import com.hipchalytics.compute.db.dao.IHipChalyticsDao;
import com.hipchalytics.compute.hipchat.dao.HipChatApiDaoFactory;
import com.hipchalytics.compute.hipchat.dao.IHipChatApiDao;
import com.hipchalytics.compute.util.YamlUtils;
import com.hipchalytics.core.model.FatMessage;
import com.hipchalytics.core.model.Message;
import com.hipchalytics.core.model.Room;
import com.hipchalytics.core.model.User;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
 * Spout that pulls messages from the hipchat API and emits {@link FatMessage}s to subscribed bolts.
 *
 * @author giannis
 *
 */
public class HipChatMessageSpout extends BaseRichSpout {

    private IHipChatApiDao hipchatDao;
    private DateTimeZone dtz;
    private SpoutOutputCollector collector;
    private IHipChalyticsDao dbDao;

    public static final String SPOUT_ID = "HIP_CHAT_MESSAGE_SPOUT_ID";
    public static final String HIPCHAT_MESSAGE_FIELD_STR = "hipchat-message";
    private static final Logger LOG = LoggerFactory.getLogger(HipChatMessageSpout.class);
    private static final long serialVersionUID = -1059483249301395751L;

    @Override
    public void nextTuple() {
        DateTime newPullEndDate = truncateDateTimeToHour(DateTime.now(dtz));
        DateTime lastPullTime = truncateDateTimeToHour(dbDao.getLastMessagePullTime());
        if (lastPullTime.isEqual(newPullEndDate) || lastPullTime.isAfter(newPullEndDate)) {
            LOG.info("Not ready to pull data yet. Last pull time was {}, new pull end date was {}",
                     lastPullTime, newPullEndDate);
            return;
        }
        Map<Integer, Room> rooms = hipchatDao.getRooms();
        List<FatMessage> messagesToEmit = Lists.newArrayList();
        for (Room room : rooms.values()) {
            List<Message> messages = hipchatDao.getMessages(lastPullTime, newPullEndDate, room);
            Map<Integer, User> users = hipchatDao.getUsers();
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
    public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
            SpoutOutputCollector collector) {
        String configYaml = (String) conf.get(ConfigurationConstants.HIPCHALYTICS_CONFIG.txt);
        HipChalyticsConfig hconfig = YamlUtils.readYamlFromString(configYaml,
                                                                  HipChalyticsConfig.class);
        LOG.info("Loaded config...");
        hipchatDao = HipChatApiDaoFactory.getHipChatApiDao(hconfig);
        dbDao = HipChalyticsDaoFactory.getHipchalyticsDao(hconfig);
        LOG.info("Got HipChat API DAO...");
        dtz = DateTimeZone.forID(hconfig.timeZone);
        this.collector = collector;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(HIPCHAT_MESSAGE_FIELD_STR));
    }

    private DateTime truncateDateTimeToHour(DateTime dateTime) {
        return dateTime.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }

}
