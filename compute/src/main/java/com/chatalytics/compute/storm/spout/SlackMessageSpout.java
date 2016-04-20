package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.slack.dao.JsonSlackDAO;
import com.chatalytics.compute.slack.dao.SlackApiDAOFactory;
import com.chatalytics.compute.util.YamlUtils;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.FatMessage;
import com.chatalytics.core.model.Message;
import com.chatalytics.core.model.Room;
import com.chatalytics.core.model.User;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.glassfish.tyrus.container.jdk.client.JdkContainerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * Spout that pulls messages from the slack API and emits {@link FatMessage}s to subscribed bolts.
 *
 * @author giannis
 */
@ClientEndpoint(decoders = { WebSocketMessageDecoder.class })
public class SlackMessageSpout extends BaseRichSpout {

    private static final long serialVersionUID = -6294446748544704853L;
    private static final Logger LOG = LoggerFactory.getLogger(SlackMessageSpout.class);
    public static final String SPOUT_ID = "SLACK_MESSAGE_SPOUT_ID";
    public static final String SLACK_MESSAGE_FIELD_STR = "slack-message";

    private IChatApiDAO slackDao;
    private SpoutOutputCollector collector;

    private final ConcurrentLinkedQueue<FatMessage> unemittedMessages;
    private Session session;

    public SlackMessageSpout() {
        unemittedMessages = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
                     SpoutOutputCollector collector) {
        String configYaml = (String) conf.get(ConfigurationConstants.CHATALYTICS_CONFIG.txt);
        ChatAlyticsConfig config = YamlUtils.readYamlFromString(configYaml,
                                                                ChatAlyticsConfig.class);
        LOG.info("Loaded config...");

        slackDao = getChatApiDao(config);
        LOG.info("Got Slack API DAO...");

        this.collector = collector;

        WebSocketContainer webSocketContainer = getWebSocketContainer();
        openRealtimeConnection(config, webSocketContainer);
    }

    /**
     * @return The websocket container.
     */
    @VisibleForTesting
    protected WebSocketContainer getWebSocketContainer() {
        return JdkContainerProvider.getWebSocketContainer();
    }

    /**
     * @return The slack API DAO
     */
    @VisibleForTesting
    protected IChatApiDAO getChatApiDao(ChatAlyticsConfig  config) {
        return SlackApiDAOFactory.getSlackApiDao(config);
    }

    /**
     * Opens the websocket and connects to the slack realtime server
     *
     * @param config
     *            The application configuration
     * @param webSocket
     *            The websocket to be used for connecting
     */
    protected void openRealtimeConnection(ChatAlyticsConfig config, WebSocketContainer webSocket) {
        URI webSocketUri = getRealtimeWebSocketURI();
        try {
            session = webSocket.connectToServer(this, webSocketUri);
            LOG.info("RTM session created with id {}", session.getId());
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

    /**
     * Called when a new chat message event is received. A {@link FatMessage} is created and pushed
     * to a concurrent queue for consumption.
     *
     * @param message
     *            The message event
     * @param session
     *            The active websocket session
     */
    @OnMessage
    public void onMessageEvent(Message message, Session session) {
        LOG.debug("Got event {}", message);
        Map<String, User> users = slackDao.getUsers();
        Map<String, Room> rooms = slackDao.getRooms();

        User fromUser = users.get(message.getFromUserId());
        Room room = rooms.get(message.getRoomId());

        FatMessage fatMessage = new FatMessage(message, fromUser, room);
        unemittedMessages.add(fatMessage);
    }

    /**
     * Called whenever an exception occurs while the websocket session is active
     *
     * @param t
     *            The exception
     */
    @OnError
    public void onError(Throwable t) {
        LOG.error(Throwables.getStackTraceAsString(t));
    }

    /**
     * Consumes from a queue that is populated by the {@link #onMessageEvent(Message, Session)}
     * method
     */
    @Override
    public void nextTuple() {
        while (!unemittedMessages.isEmpty()) {
            FatMessage fatMessage = unemittedMessages.remove();
            collector.emit(new Values(fatMessage));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(SLACK_MESSAGE_FIELD_STR));
    }

    @Override
    public void close() {
        if (session != null) {
            try {
                session.close();
            } catch (IOException e) {
                LOG.error("Session did not close cleanly. Got {}", e.getMessage());
            }
        }
    }

}
