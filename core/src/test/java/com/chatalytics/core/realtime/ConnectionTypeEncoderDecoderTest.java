package com.chatalytics.core.realtime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.websocket.EndpointConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link ConnectionTypeEncoderDecoder}
 *
 * @author giannis
 */
public class ConnectionTypeEncoderDecoderTest {

    private ConnectionTypeEncoderDecoder underTest;

    @Before
    public void setUp() {
        underTest = new ConnectionTypeEncoderDecoder();
    }

    /**
     * Make sure an exception is not thrown if init is called
     */
    @Test
    public void testInit() {
        underTest.init(mock(EndpointConfig.class));
    }

    @Test
    public void testWillDecode() {
        assertFalse(underTest.willDecode(null));
        assertFalse(underTest.willDecode(""));
        assertTrue(underTest.willDecode("PUBLISHER"));
    }

    @Test
    public void testEncodeDecode() throws Exception {
        ConnectionType cxType = ConnectionType.PUBLISHER;
        String encodedStr = underTest.encode(cxType);
        assertEquals(cxType, underTest.decode(encodedStr));

        cxType = ConnectionType.SUBSCRIBER;
        encodedStr = underTest.encode(cxType);
        assertEquals(cxType, underTest.decode(encodedStr));
    }

    @After
    public void tearDown() {
        underTest.destroy();
    }
}
