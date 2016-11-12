package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.chat.dao.slack.JsonSlackDAO;
import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.exception.NotConnectedException;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.SlackConfig;
import com.chatalytics.core.model.data.Message;
import com.chatalytics.core.model.data.MessageType;
import com.chatalytics.core.model.data.Room;
import com.chatalytics.core.model.data.User;
import com.chatalytics.core.util.YamlUtils;
import com.google.common.collect.Maps;

import org.apache.storm.shade.com.google.common.collect.ImmutableMap;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import static com.chatalytics.core.model.data.MessageType.MESSAGE;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SlackMessageSpout}.
 *
 * @author giannis
 *
 */
public class SlackMessageSpoutTest {

    private static final URI WEB_SOCKET_TEST_URI = URI.create("test://realtime.test");

    private SlackMessageSpout underTest;
    private SpoutOutputCollector mockCollector;
    private TopologyContext mockContext;
    private HashMap<String, String> stormConf;

    private ChatAlyticsConfig config;

    private SlackConfig chatConfig;

    @Before
    public void setUp() throws Exception {
        underTest = new SlackMessageSpout();
        mockCollector = mock(SpoutOutputCollector.class);
        mockContext = mock(TopologyContext.class);
        stormConf = Maps.newHashMapWithExpectedSize(1);
        config = new ChatAlyticsConfig();
        chatConfig = new SlackConfig();
        chatConfig.sourceConnectionMaxMs = 0;
        chatConfig.sourceConnectionSleepIntervalMs = 1;
        config.computeConfig.chatConfig = chatConfig;
    }

    @Test(expected = NotConnectedException.class)
    public void testOpen_withAuthException() throws Exception {
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        try {
            underTest.open(stormConf, mockContext, mockCollector);
            fail();
        } catch (Exception e) {
            throw (Exception) e.getCause();
        }
    }

    /**
     * Tests to see if an exception is propagated when the spout can't establish a socket connection
     * to the slack server
     */
    @Test(expected = DeploymentException.class)
    public void testOpen_withDeploymentException() throws Exception {
        WebSocketContainer webSocket = mock(WebSocketContainer.class);
        JsonSlackDAO slackDao = mock(JsonSlackDAO.class);
        when(slackDao.getRealtimeWebSocketURI()).thenReturn(WEB_SOCKET_TEST_URI);
        when(webSocket.connectToServer(underTest, WEB_SOCKET_TEST_URI))
                .thenThrow(new DeploymentException("broken"));
        try {
            underTest.open(chatConfig, slackDao, webSocket, mockContext, mockCollector);
            fail();
        } catch (Exception e) {
            throw (Exception) e.getCause();
        }
    }

    @Test(expected = NotConnectedException.class)
    public void testOpen_withExceptionAndRetries() throws Exception {
        chatConfig.sourceConnectionSleepIntervalMs = 1000;
        chatConfig.sourceConnectionMaxMs = 1000;
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        try {
            underTest.open(stormConf, mockContext, mockCollector);
            fail();
        } catch (Exception e) {
            throw (Exception) e.getCause();
        }
    }

    /**
     * Tests the {@link SlackMessageSpout#onMessageEvent(Message, Session)} method and makes sure
     * that a fatMessage is created and then pushed to a queue for consumption. This test also calls
     * {@link SlackMessageSpout#nextTuple()} to see if the fat message is properly consumed from the
     * queue.
     */
    @Test
    public void testOnMessageEvent() throws Exception {
        // setup mocks
        WebSocketContainer mockSocketContainer = mock(WebSocketContainer.class);
        when(mockSocketContainer.connectToServer(underTest, WEB_SOCKET_TEST_URI))
            .thenReturn(mock(Session.class));
        JsonSlackDAO slackDao = mock(JsonSlackDAO.class);
        when(slackDao.getRealtimeWebSocketURI()).thenReturn(WEB_SOCKET_TEST_URI);
        underTest.open(chatConfig, slackDao, mockSocketContainer, mockContext, mockCollector);

        String userId = "U03AFSSD";

        // make the chat API DAO return a map of users
        Map<String, User> users = ImmutableMap.of(userId,
                                                  new User(userId, null, false, false,  false,
                                                           "name", "mention_name", null,
                                                           DateTime.now(), DateTime.now(), null,
                                                           null, null, null));
        when(slackDao.getUsers()).thenReturn(users);

        Message triggerMessage = new Message(DateTime.now(), "Test User", userId, "test msg",
                                             "C09ADF43", MESSAGE);

        underTest.onMessageEvent(triggerMessage, mock(Session.class));
        verify(slackDao).getUsers();
        verify(slackDao).getRooms();
        verify(slackDao).getRealtimeWebSocketURI();
        verifyNoMoreInteractions(slackDao);
        underTest.nextTuple();
        verify(mockCollector).emit(any(Values.class));
        verifyNoMoreInteractions(mockCollector);

        // make sure nothing got emitted
        underTest.nextTuple();
        verifyNoMoreInteractions(mockCollector);
    }

    @Test
    public void testOnMessageEvent_botUser() throws Exception {
        JsonSlackDAO slackDao = mock(JsonSlackDAO.class);
        Map<String, User> users = ImmutableMap.of("u1", new User("u1", "email", false, false, false,
                                                                 "name", "mention_name", null,
                                                                 DateTime.now(), DateTime.now(),
                                                                 null, null, null, null));
        Map<String, Room> rooms = ImmutableMap.of("r1", new Room("r1", "room", null, DateTime.now(),
                                                                 DateTime.now(), null, false, false,
                                                                 null, null));
        when(slackDao.getUsers()).thenReturn(users);
        when(slackDao.getRooms()).thenReturn(rooms);
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("id");
        when(slackDao.getRealtimeWebSocketURI()).thenReturn(WEB_SOCKET_TEST_URI);
        WebSocketContainer webSocket = mock(WebSocketContainer.class);
        when(webSocket.connectToServer(underTest, WEB_SOCKET_TEST_URI)).thenReturn(session);
        underTest.open(chatConfig, slackDao, webSocket, mockContext, mockCollector);

        Message triggerMessage = new Message(DateTime.now(), "BotUser", "b1", "test msg",
                                             "r1", MessageType.BOT_MESSAGE);
        underTest.onMessageEvent(triggerMessage, mock(Session.class));
        verify(slackDao).getUsers();
        verify(slackDao).getRooms();
        verify(slackDao).getRealtimeWebSocketURI();
        verifyNoMoreInteractions(slackDao);
        underTest.nextTuple();
        verify(mockCollector).emit(any(Values.class));
        verifyNoMoreInteractions(mockCollector);
    }

    @Test
    public void testOnMessageEvent_nullUser() throws Exception {
        JsonSlackDAO slackDao = mock(JsonSlackDAO.class);
        Map<String, User> users = ImmutableMap.of("u1", new User("u1", "email", false, false, false,
                                                                 "name", "mention_name", null,
                                                                 DateTime.now(), DateTime.now(),
                                                                 null, null, null, null));
        Map<String, Room> rooms = ImmutableMap.of("r1", new Room("r1", "room", null, DateTime.now(),
                                                                 DateTime.now(), null, false, false,
                                                                 null, null));
        when(slackDao.getUsers()).thenReturn(users);
        when(slackDao.getRooms()).thenReturn(rooms);
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("id");
        when(slackDao.getRealtimeWebSocketURI()).thenReturn(WEB_SOCKET_TEST_URI);
        WebSocketContainer webSocket = mock(WebSocketContainer.class);
        when(webSocket.connectToServer(underTest, WEB_SOCKET_TEST_URI)).thenReturn(session);
        underTest.open(chatConfig, slackDao, webSocket, mockContext, mockCollector);

        Message triggerMessage = new Message(DateTime.now(), "BotUser", "u2", "test msg", "r1",
                                             MESSAGE);
        underTest.onMessageEvent(triggerMessage, mock(Session.class));
        verify(slackDao).getUsers();
        verify(slackDao).getRooms();
        verify(slackDao).getRealtimeWebSocketURI();
        verifyNoMoreInteractions(slackDao);
        underTest.nextTuple();
        verifyZeroInteractions(mockCollector);
    }

    @Test
    public void testOnMessageEvent_nullRoom() throws Exception {
        JsonSlackDAO slackDao = mock(JsonSlackDAO.class);
        Map<String, User> users = ImmutableMap.of("u1", new User("u1", "email", false, false, false,
                                                                 "name", "mention_name", null,
                                                                 DateTime.now(), DateTime.now(),
                                                                 null, null, null, null));
        Map<String, Room> rooms = ImmutableMap.of();
        when(slackDao.getUsers()).thenReturn(users);
        when(slackDao.getRooms()).thenReturn(rooms);
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("id");
        when(slackDao.getRealtimeWebSocketURI()).thenReturn(WEB_SOCKET_TEST_URI);
        WebSocketContainer webSocket = mock(WebSocketContainer.class);
        when(webSocket.connectToServer(underTest, WEB_SOCKET_TEST_URI)).thenReturn(session);
        underTest.open(chatConfig, slackDao, webSocket, mockContext, mockCollector);

        // this was a direct chat message
        Message triggerMessage = new Message(DateTime.now(), "name", "u1", "test msg",
                                             "D1R3CTM355", MESSAGE);
        underTest.onMessageEvent(triggerMessage, mock(Session.class));

        verify(slackDao).getUsers();
        verify(slackDao).getRooms();
        verify(slackDao).getRealtimeWebSocketURI();
        verifyNoMoreInteractions(slackDao);
        underTest.nextTuple();
        verify(mockCollector).emit(any(Values.class));
        verifyNoMoreInteractions(mockCollector);

        // make sure nothing got emitted
        underTest.nextTuple();
        verifyNoMoreInteractions(mockCollector);
    }

    @Test
    public void testOnMessageEvent_withStatDate() throws Exception {
        JsonSlackDAO slackDao = mock(JsonSlackDAO.class);
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("id");
        when(slackDao.getRealtimeWebSocketURI()).thenReturn(WEB_SOCKET_TEST_URI);
        WebSocketContainer webSocket = mock(WebSocketContainer.class);
        when(webSocket.connectToServer(underTest, WEB_SOCKET_TEST_URI)).thenReturn(session);

        DateTime startDate = DateTime.now();
        chatConfig.startDate = startDate.toString();

        // open with a start date
        underTest.open(chatConfig, slackDao, webSocket, mockContext, mockCollector);

        DateTime messageDate = startDate.minusHours(1);
        Message triggerMessage = new Message(messageDate, "name", "u1", "test msg", "D1R3CTM355",
                                             MESSAGE);
        underTest.onMessageEvent(triggerMessage, session);
        verify(slackDao).getRealtimeWebSocketURI();
        verifyNoMoreInteractions(slackDao);
        underTest.nextTuple();
        verifyZeroInteractions(mockCollector);

        // try again with a message date that's after the start date
        messageDate = startDate.plusHours(1);
        Map<String, User> users = ImmutableMap.of("u1", new User("u1", "email", false, false, false,
                                                                 "name", "mention_name", null,
                                                                 DateTime.now(), DateTime.now(),
                                                                 null, null, null, null));
        Map<String, Room> rooms = ImmutableMap.of();
        when(slackDao.getUsers()).thenReturn(users);
        when(slackDao.getRooms()).thenReturn(rooms);
        triggerMessage = new Message(messageDate, "name", "u1", "test msg", "D1R3CTM355", MESSAGE);
        underTest.onMessageEvent(triggerMessage, session);
        verify(slackDao).getUsers();
        verify(slackDao).getRooms();
        verify(slackDao).getRealtimeWebSocketURI();
        verifyNoMoreInteractions(slackDao);
        underTest.nextTuple();
        verify(mockCollector).emit(any(Values.class));
        verifyNoMoreInteractions(mockCollector);

        // try again with a message date that is equal to the start date
        reset(slackDao);
        messageDate = startDate;
        triggerMessage = new Message(messageDate, "name", "u1", "test msg", "D1R3CTM355", MESSAGE);
        underTest.onMessageEvent(triggerMessage, session);
        verify(slackDao).getUsers();
        verify(slackDao).getRooms();
        verifyNoMoreInteractions(slackDao);
        underTest.nextTuple();
        verify(mockCollector).emit(any(Values.class));
        verifyNoMoreInteractions(mockCollector);
    }

    @Test
    public void testDeclareOutputFields() {
        OutputFieldsDeclarer mockFields = mock(OutputFieldsDeclarer.class);
        underTest.declareOutputFields(mockFields);
        verify(mockFields).declare(any(Fields.class));
    }

    @Test
    public void testOnError() {
        underTest.onError(new RuntimeException("test"));
    }

    @After
    public void tearDown() throws Exception {
        underTest.close();
    }

}
