package com.hipchalytics.storm.spout;

import com.google.common.collect.Lists;
import com.hipchalytics.config.ConfigurationConstants;
import com.hipchalytics.config.HipChalyticsConfig;
import com.hipchalytics.model.FatMessage;
import com.hipchalytics.model.Message;
import com.hipchalytics.model.Room;
import com.hipchalytics.model.User;
import com.hipchalytics.storm.dao.HipChatApiDaoFactory;
import com.hipchalytics.storm.dao.IHipChatApiDao;
import com.hipchalytics.util.YamlUtils;

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
    private DateTime lastPullTime;
    private SpoutOutputCollector collector;

    public static final String SPOUT_ID = "HIP_CHAT_MESSAGE_SPOUT_ID";
    public static final String HIPCHAT_MESSAGE_FIELD_STR = "hipchat-message";
    private static final Logger LOG = LoggerFactory.getLogger(HipChatMessageSpout.class);
    private static final long serialVersionUID = -1059483249301395751L;

    @Override
    public void nextTuple() {
        Map<Integer, Room> rooms = hipchatDao.getRooms();
        DateTime endDate = roundDateTime(new DateTime(System.currentTimeMillis()).withZone(dtz));
        if (lastPullTime.isEqual(endDate) || lastPullTime.isAfter(endDate)) {
            return;
        }
        List<FatMessage> messagesToEmit = Lists.newArrayList();
        for (Room room : rooms.values()) {
            List<Message> messages = hipchatDao.getMessages(lastPullTime, endDate, room);
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
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        String configYaml = (String) conf.get(ConfigurationConstants.HIPCHALYTICS_CONFIG.txt);
        HipChalyticsConfig hconfig = YamlUtils.readYamlFromString(configYaml,
                                                                  HipChalyticsConfig.class);
        LOG.info("Loaded config...");
        hipchatDao = HipChatApiDaoFactory.getHipChatApiDao(hconfig);
        LOG.info("Got HipChat API DAO...");
        dtz = DateTimeZone.forID(hconfig.timeZone);
        lastPullTime = roundDateTime(new DateTime(System.currentTimeMillis()).withZone(dtz));
        this.collector = collector;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(HIPCHAT_MESSAGE_FIELD_STR));
    }

    private DateTime roundDateTime(DateTime dateTime) {
        return dateTime.withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }

}
