package com.chatalytics.compute.web.realtime;

import com.chatalytics.core.config.ChatAlyticsConfig;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link ComputeRealtimeServer}
 *
 * @author giannis
 *
 */
public class ComputeRealtimeServerFactoryTest {

    private ComputeRealtimeServerFactory underTest;

    @Before
    public void setUp() {
        ChatAlyticsConfig config = new ChatAlyticsConfig();
        underTest = new ComputeRealtimeServerFactory(config);
    }

    @Test
    public void testCreateComputeRealtimeServer() {
        ComputeRealtimeServer server = underTest.createComputeRealtimeServer();
        assertNotNull(server);
        server.startAsync().awaitRunning();
        server.stopAsync().awaitTerminated();
    }
}
