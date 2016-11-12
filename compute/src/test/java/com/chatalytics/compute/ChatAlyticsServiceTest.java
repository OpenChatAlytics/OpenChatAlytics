package com.chatalytics.compute;

import com.chatalytics.compute.storm.ChatAlyticsStormTopology;
import com.chatalytics.compute.web.realtime.ComputeRealtimeServer;
import com.chatalytics.compute.web.realtime.ComputeRealtimeServerFactory;
import com.chatalytics.core.InputSourceType;
import com.chatalytics.core.config.ChatAlyticsConfig;

import org.apache.storm.generated.StormTopology;
import org.eclipse.jetty.server.Server;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ChatAlyticsService}.
 *
 * @author giannis
 *
 */
public class ChatAlyticsServiceTest {

    @Test
    public void testStartUpShutDown() throws Exception {
        ChatAlyticsConfig conf = new ChatAlyticsConfig();
        conf.inputType = InputSourceType.LOCAL_TEST;
        StormTopology stormTopology = ChatAlyticsStormTopology.create(conf);
        ComputeRealtimeServerFactory rtServerFactory = mock(ComputeRealtimeServerFactory.class);
        ComputeRealtimeServer computeRTServer = new ComputeRealtimeServer(new Server());
        when(rtServerFactory.createComputeRealtimeServer()).thenReturn(computeRTServer);
        ChatAlyticsService underTest = new ChatAlyticsService(stormTopology, rtServerFactory, conf);
        underTest.startUp();

        assertTrue(computeRTServer.isRunning());
        verify(rtServerFactory).createComputeRealtimeServer();
        verifyNoMoreInteractions(rtServerFactory);

        underTest.shutDown();
        assertFalse(computeRTServer.isRunning());
    }

    @Test
    public void testStartUpShutDown_noRTServer() throws Exception {
        ChatAlyticsConfig conf = new ChatAlyticsConfig();
        conf.inputType = InputSourceType.LOCAL_TEST;
        StormTopology stormTopology = ChatAlyticsStormTopology.create(conf);
        conf.computeConfig.enableRealtimeEvents = false;
        ComputeRealtimeServerFactory rtServerFactory = mock(ComputeRealtimeServerFactory.class);
        ChatAlyticsService underTest = new ChatAlyticsService(stormTopology, rtServerFactory, conf);
        underTest.startUp();

        verifyZeroInteractions(rtServerFactory);

        underTest.shutDown();
    }

}
