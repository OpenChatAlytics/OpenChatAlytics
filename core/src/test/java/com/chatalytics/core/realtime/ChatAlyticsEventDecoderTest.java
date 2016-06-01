package com.chatalytics.core.realtime;

import com.chatalytics.core.model.data.ChatAlyticsEvent;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.model.data.MessageType;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.DecodeException;
import javax.websocket.EndpointConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link ChatAlyticsEventDecoder}
 *
 * @author giannis
 */
public class ChatAlyticsEventDecoderTest {

    private ChatAlyticsEventDecoder underTest;

    @Before
    public void setUp() {
        underTest = new ChatAlyticsEventDecoder();
    }

    /**
     * Make sure an exception is not thrown if init is called
     */
    @Test
    public void testInit() {
        underTest.init(mock(EndpointConfig.class));
    }

    @Test
    public void testDecode() throws Exception {
        DateTime eventTime = DateTime.now(DateTimeZone.UTC);
        MessageSummary msgSummary = new MessageSummary("user", "room",
                                                       DateTime.now(DateTimeZone.UTC),
                                                       MessageType.BOT_MESSAGE, 1);
        ChatAlyticsEvent event = new ChatAlyticsEvent(eventTime, msgSummary.getClass().getName(),
                                                      msgSummary);
        String jsonStr = new ChatAlyticsEventEncoder().encode(event);
        ChatAlyticsEvent decodedEvent = underTest.decode(jsonStr);
        assertEquals(event, decodedEvent);
    }

    @Test(expected = DecodeException.class)
    public void testDecode_withBadJson() throws Exception {
        underTest.decode("bad json");
    }

    @Test
    public void testWillDecode() {
        assertFalse(underTest.willDecode(null));
        assertFalse(underTest.willDecode(""));
        assertTrue(underTest.willDecode("{}"));
    }

    @After
    public void tearDown() {
        underTest.destroy();
    }
}
