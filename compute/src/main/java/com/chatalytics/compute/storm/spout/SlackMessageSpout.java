package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.db.dao.ChatAlyticsDAO;
import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.slack.dao.JsonSlackDAO;
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

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnMessage;
import javax.websocket.WebSocketContainer;

/**
 * Spout that pulls messages from the slack API and emits {@link FatMessage}s to subscribed bolts.
 *
 * @author giannis
 */
@ClientEndpoint
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
        LOG.info("Got Slack API DAO...");

        dbDao = ChatAlyticsDAOFactory.getChatAlyticsDao(config);
        LOG.info("Got database DAO...");

        this.collector = collector;

        WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        openRealtimeConnection(config, webSocketContainer);
    }

    public void openRealtimeConnection(ChatAlyticsConfig config, WebSocketContainer webSocket) {
        webSocket = ContainerProvider.getWebSocketContainer();
        URI webSocketUri = getRealtimeWebSocketURI();
        try {
            webSocket.connectToServer(this, webSocketUri);
        } catch (DeploymentException | IOException e) {
            String errMsg = String.format("Unable to connect to %s", webSocketUri);
            LOG.error(errMsg);
            throw new RuntimeException(errMsg, e);
        }
    }

    /**
     * Gets the realtime web socket URI first by checking to see if the current implementation of
     * the {@link IChatApiDAO} supports this
     *
     * @return The web socket URI to connect to the realtime slack message stream
     */
    protected URI getRealtimeWebSocketURI() {
        if (slackDao instanceof JsonSlackDAO) {
            return ((JsonSlackDAO) slackDao).getRealtimeWebSocketURI();
        } else {
            throw new RuntimeException("Current Slack API DAO implementation cannot be used");
        }
    }

    @OnMessage
    protected  void onEvent(String event) {
        System.out.println(event);
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
