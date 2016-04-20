package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.util.YamlUtils;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.Message;
import com.google.common.collect.Maps;

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

import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

    @Before
    public void setUp() throws Exception {
        underTest = spy(new SlackMessageSpout());
        mockCollector = mock(SpoutOutputCollector.class);
        mockContext = mock(TopologyContext.class);
        stormConf = Maps.newHashMapWithExpectedSize(1);
        config = YamlUtils.readYamlFromResource("chatalytics.yaml", ChatAlyticsConfig.class);

        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
    }

    /**
     * Makes sure a connection can be established with the slack realtime server
     */
    @Test
    public void testOpen() throws Exception {
        WebSocketContainer mockSocketContainer = mock(WebSocketContainer.class);
        when(mockSocketContainer.connectToServer(underTest, WEB_SOCKET_TEST_URI))
            .thenReturn(mock(Session.class));

        doReturn(WEB_SOCKET_TEST_URI).when(underTest).getRealtimeWebSocketURI();
        doReturn(mockSocketContainer).when(underTest).getWebSocketContainer();

        underTest.open(stormConf, mockContext, mockCollector);

        verify(mockSocketContainer).connectToServer(underTest, WEB_SOCKET_TEST_URI);
    }

    /**
     * Tests to see if an exception is propagated when the spout can't establish a socket connection
     * to the slack server
     */
    @Test(expected = DeploymentException.class)
    public void testOpen_withException() throws Exception {
        doReturn(WEB_SOCKET_TEST_URI).when(underTest).getRealtimeWebSocketURI();
        try {
            underTest.open(stormConf, mockContext, mockCollector);
        } catch (Exception e) {
            throw (DeploymentException) e.getCause();
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
        doReturn(WEB_SOCKET_TEST_URI).when(underTest).getRealtimeWebSocketURI();
        doReturn(mockSocketContainer).when(underTest).getWebSocketContainer();

        IChatApiDAO mockSlackApiDao = mock(IChatApiDAO.class);
        doReturn(mockSlackApiDao).when(underTest).getChatApiDao(any(ChatAlyticsConfig.class));
        underTest.open(stormConf, mockContext, mockCollector);

        // trigger with this test message
        Message triggerMessage = new Message(DateTime.now(), "Test User", "U03AFSSD", "test msg",
                                             "C09ADF43");
        underTest.onMessageEvent(triggerMessage, mock(Session.class));
        verify(mockSlackApiDao).getUsers();
        verify(mockSlackApiDao).getRooms();

        underTest.nextTuple();
        verify(mockCollector).emit(any(Values.class));

        underTest.nextTuple();
        verifyNoMoreInteractions(mockCollector);
    }

    @Test
    public void testDeclareOutputFields() {
        OutputFieldsDeclarer mockFields = mock(OutputFieldsDeclarer.class);
        underTest.declareOutputFields(mockFields);
        verify(mockFields).declare(any(Fields.class));
    }

    @After
    public void tearDown() throws Exception {
        underTest.close();
    }

}
