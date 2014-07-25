package com.hipchalytics.storm.spout;

import com.hipchalytics.config.ConfigurationConstants;
import com.hipchalytics.config.HipChalyticsConfig;
import com.hipchalytics.model.FatMessage;
import com.hipchalytics.storm.dao.HipChatApiDaoFactory;
import com.hipchalytics.storm.dao.IHipChatApiDao;
import com.hipchalytics.util.YamlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;

import java.util.Map;

/**
 * Spout that pulls messages from the hipchat API and emits {@link FatMessage}s to subscribed bolts.
 *
 * @author giannis
 *
 */
public class HipChatMessageSpout extends BaseRichSpout {

    private IHipChatApiDao hipchatDao;

    public static final String SPOUT_ID = "HIP_CHAT_MESSAGE_SPOUT_ID";

    private static final Logger LOG = LoggerFactory.getLogger(HipChatMessageSpout.class);

    private static final long serialVersionUID = -1059483249301395751L;

    public HipChatMessageSpout() {

    }

    @Override
    public void nextTuple() {
        hipchatDao.getRooms();
        hipchatDao.getUsers();
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        String configYaml = (String) conf.get(ConfigurationConstants.HIPCHALYTICS_CONFIG.txt);
        HipChalyticsConfig hconfig = YamlUtils.readYamlFromString(configYaml,
                                                                  HipChalyticsConfig.class);
        LOG.info("Loaded config...");
        hipchatDao = HipChatApiDaoFactory.getHipChatApiDao(hconfig);
        LOG.info("Got HipChat API DAO...");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        // TODO Auto-generated method stub

    }

}
