package com.chatalytics.core.realtime;

import com.chatalytics.core.model.data.ChatAlyticsEvent;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.model.data.MessageType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link ChatAlyticsEventEncoder}
 *
 * @author giannis
 */
public class ChatAlyticsEventEncoderTest {

    private ChatAlyticsEventEncoder underTest;

    @Before
    public void setUp() {
        underTest = new ChatAlyticsEventEncoder();
    }

    /**
     * Make sure an exception is not thrown when init is called
     */
    @Test
    public void testInit() {
        underTest.init(mock(EndpointConfig.class));
    }

    @Test
    public void testEncode() throws Exception {
        DateTime eventTime = DateTime.now(DateTimeZone.UTC);
        MessageSummary msgSummary = new MessageSummary("user", "room",
                                                       DateTime.now(DateTimeZone.UTC),
                                                       MessageType.BOT_MESSAGE, 1);
        ChatAlyticsEvent event = new ChatAlyticsEvent(eventTime, msgSummary.getClass().getName(),
                                                      msgSummary);
        String jsonStr = underTest.encode(event);
        ChatAlyticsEvent decodedEvent = new ChatAlyticsEventDecoder().decode(jsonStr);
        assertEquals(event, decodedEvent);
    }

    @Test(expected = EncodeException.class)
    public void testEncode_badSerializable() throws Exception {
        underTest.encode(new ChatAlyticsEvent(DateTime.now(), "type", mock(MessageSummary.class)));
    }

    @After
    public void tearDown() {
        underTest.destroy();
    }
}
