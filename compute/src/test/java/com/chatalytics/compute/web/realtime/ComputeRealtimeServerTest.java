package com.chatalytics.compute.web.realtime;

import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link ComputeRealtimeServer}
 *
 * @author giannis
 *
 */
public class ComputeRealtimeServerTest {

    private Server server;
    private ComputeRealtimeServer underTest;

    @Before
    public void setUp() {
        server = new Server(1234);
        underTest = new ComputeRealtimeServer(server);
    }

    @Test
    public void testStartUp() throws Exception {
        underTest.startUp();
        assertTrue(server.isRunning());
        underTest.shutDown();
        assertFalse(server.isRunning());
    }

    @Test
    public void testShutDown() throws Exception {
        underTest.startUp();
        underTest.shutDown();
        assertFalse(server.isRunning());
    }

}
