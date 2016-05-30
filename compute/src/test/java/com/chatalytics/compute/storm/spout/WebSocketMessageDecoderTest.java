package com.chatalytics.compute.storm.spout;

import com.chatalytics.core.model.data.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.DecodeException;
import javax.websocket.EndpointConfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests the {@link WebSocketMessageDecoder}.
 *
 * @author giannis
 *
 */
public class WebSocketMessageDecoderTest {

    private WebSocketMessageDecoder underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new WebSocketMessageDecoder();
        underTest.init(mock(EndpointConfig.class));
    }

    /**
     * Checks to see if a message is returned when decode is called on a valid string
     */
    @Test
    public void testDecode() throws Exception {
        Message message = underTest.decode(messageJsonStr);
        assertNotNull(message);
    }

    /**
     * Checks to see if the decoder propagates a {@link DecodeException} when an invalid message
     * string is passed.
     */
    @Test (expected = DecodeException.class)
    public void testDecode_withException() throws Exception {
        underTest.decode("broken event");
    }

    /**
     * Tests various event strings to make sure that only supported event strings will get decoded
     */
    @Test
    public void testWillDecode() {
        assertTrue(underTest.willDecode(messageJsonStr));
        assertFalse(underTest.willDecode("{\"type\": \"hello\"}"));
        assertFalse(underTest.willDecode("bad message string"));
    }

    @After
    public void tearDown() throws Exception {
        underTest.destroy();
    }

    private final String messageJsonStr = "{" +
                                              "\"type\": \"message\"," +
                                              "\"user\": \"U023BECGF\"," +
                                              "\"text\": \"test message\"," +
                                              "\"ts\": \"1431708451.000186\"" +
                                          "}";

}
