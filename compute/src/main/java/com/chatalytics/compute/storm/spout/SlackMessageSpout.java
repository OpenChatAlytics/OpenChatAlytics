package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.db.dao.ChatAlyticsDAO;
import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.slack.dao.SlackApiDAOFactory;
import com.chatalytics.compute.util.YamlUtils;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.FatMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;

import java.util.Map;

/**
 * Spout that pulls messages from the slack API and emits {@link FatMessage}s to subscribed bolts.
 *
 * @author giannis
 */
public class SlackMessageSpout extends BaseRichSpout {

    private static final long serialVersionUID = -6294446748544704853L;
    private static final Logger LOG = LoggerFactory.getLogger(SlackMessageSpout.class);
    public static final String SPOUT_ID = "SLACK_MESSAGE_SPOUT_ID";
    public static final String SLACK_MESSAGE_FIELD_STR = "slack-message";

    private IChatApiDAO slackDao;
    private ChatAlyticsDAO dbDao;
    private SpoutOutputCollector collector;

    @Override
    public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
                    SpoutOutputCollector collector) {
        String configYaml = (String) conf.get(ConfigurationConstants.CHATALYTICS_CONFIG.txt);
        ChatAlyticsConfig config = YamlUtils.readYamlFromString(configYaml,
                                                                ChatAlyticsConfig.class);
        LOG.info("Loaded config...");

        slackDao = SlackApiDAOFactory.getSlackApiDao(config);
        LOG.info("Got HipChat API DAO...");

        dbDao = ChatAlyticsDAOFactory.getChatAlyticsDao(config);
        LOG.info("Got database DAO...");

        this.collector = collector;
    }

    @Override
    public void nextTuple() {
        // TODO Auto-generated method stub

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(SLACK_MESSAGE_FIELD_STR));
    }

}
