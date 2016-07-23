package com.chatalytics.web.resources;

import com.chatalytics.core.model.data.ChatAlyticsEvent;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import static com.chatalytics.compute.web.realtime.RealtimeResource.RT_COMPUTE_ENDPOINT;
import static com.chatalytics.web.resources.EventsResource.RT_EVENT_ENDPOINT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link EventsResource}
 *
 * @author giannis
 */
public class EventsResourceTest {

    private EventsResource underTest;

    @Before
    public void setUp() {
        underTest = new EventsResource();
    }

    /**
     * Tests the on open method when the events resource is not yet connected with the compute
     * server
     */
    @Test
    public void testOnOpen_diconnectedFromCompute() throws Exception {
        Session session = mock(Session.class);
        when(session.getRequestURI()).thenReturn(URI.create("http://fake" + RT_EVENT_ENDPOINT));
        underTest.onOpen(session);
        verify(session).close(any(CloseReason.class));
        assertEquals(0, underTest.getSessions().size());
    }

    /**
     * Tests the open method when the resource is not connected to the compute server to see if a
     * problematic session is passed in that throws an exception on close doesn't cause an exception
     * to bubble up
     */
    @Test
    public void testOnOpen_disconnectedFromComputeAndFailToClose() throws Exception {
        Session session = mock(Session.class);
        when(session.getRequestURI()).thenReturn(URI.create("http://fake" + RT_EVENT_ENDPOINT));
        doThrow(IOException.class).when(session).close(any(CloseReason.class));
        underTest.onOpen(session);
        verify(session).close(any(CloseReason.class));
        assertEquals(0, underTest.getSessions().size());
    }

    /**
     * Tests to see if a client session is correctly opened when open is called after the events
     * resource connects to the compute server
     */
    @Test
    public void testOnOpen_afterConnectingToCompute() {
        Session computeSession = mock(Session.class);
        URI computeURI = URI.create("http://fake" + RT_COMPUTE_ENDPOINT);
        when(computeSession.getRequestURI()).thenReturn(computeURI);
        underTest.onOpen(computeSession);
        assertEquals(0, underTest.getSessions().size());
        verify(computeSession).getRequestURI();
        verifyNoMoreInteractions(computeSession);
        assertTrue(underTest.isConnectedToCompute());

        // open first client session
        Session firstClientSession = mock(Session.class);
        URI resourceURI = URI.create("http://fake" + RT_EVENT_ENDPOINT);
        when(firstClientSession.getRequestURI()).thenReturn(resourceURI);
        underTest.onOpen(firstClientSession);
        verify(firstClientSession).getRequestURI();
        verify(firstClientSession).getId();
        verifyNoMoreInteractions(firstClientSession);
        assertEquals(1, underTest.getSessions().size());
        assertEquals(firstClientSession, underTest.getSessions().iterator().next());

        // close the first session
        when(firstClientSession.isOpen()).thenReturn(false);
        // open second client session
        Session secondClientSession = mock(Session.class);
        when(secondClientSession.getRequestURI()).thenReturn(resourceURI);
        underTest.onOpen(secondClientSession);
        verify(secondClientSession).getRequestURI();
        verify(secondClientSession).getId();
        verifyNoMoreInteractions(secondClientSession);
        assertEquals(1, underTest.getSessions().size());
        assertEquals(secondClientSession, underTest.getSessions().iterator().next());

        // add a third session
        when(secondClientSession.isOpen()).thenReturn(true);
        Session thirdClientSession = mock(Session.class);
        when(thirdClientSession.getRequestURI()).thenReturn(resourceURI);
        underTest.onOpen(thirdClientSession);
        verify(secondClientSession).isOpen();
        verifyNoMoreInteractions(secondClientSession);
        verify(thirdClientSession).getRequestURI();
        verify(thirdClientSession).getId();
        verifyNoMoreInteractions(thirdClientSession);
        assertEquals(2, underTest.getSessions().size());

        Set<Session> sessions = underTest.getSessions();
        assertTrue(sessions.contains(secondClientSession));
        assertTrue(sessions.contains(thirdClientSession));
    }

    /**
     * Checks to see if the compute session is closed properly
     */
    @Test
    public void testClose_fromCompute() {
        Session computeSession = mock(Session.class);
        URI computeURI = URI.create("http://fake" + RT_COMPUTE_ENDPOINT);
        when(computeSession.getRequestURI()).thenReturn(computeURI);

        underTest.onOpen(computeSession);
        assertTrue(underTest.isConnectedToCompute());

        underTest.close(computeSession, mock(CloseReason.class));
        assertFalse(underTest.isConnectedToCompute());

        verify(computeSession, times(2)).getRequestURI();
        verifyNoMoreInteractions(computeSession);
    }

    /**
     * Makes sure a client session is properly closed and removed from the list of session when the
     * close method is invoked
     */
    @Test
    public void testClose_fromClient() throws Exception {
        Session computeSession = mock(Session.class);
        URI computeURI = URI.create("http://fake" + RT_COMPUTE_ENDPOINT);
        when(computeSession.getRequestURI()).thenReturn(computeURI);
        underTest.onOpen(computeSession);
        assertEquals(0, underTest.getSessions().size());
        verify(computeSession).getRequestURI();
        verifyNoMoreInteractions(computeSession);
        assertTrue(underTest.isConnectedToCompute());

        // open first client session
        Session firstClientSession = mock(Session.class);
        URI resourceURI = URI.create("http://fake" + RT_EVENT_ENDPOINT);
        when(firstClientSession.getRequestURI()).thenReturn(resourceURI);
        underTest.onOpen(firstClientSession);
        verify(firstClientSession).getRequestURI();
        verify(firstClientSession).getId();
        verifyNoMoreInteractions(firstClientSession);
        assertEquals(1, underTest.getSessions().size());
        assertEquals(firstClientSession, underTest.getSessions().iterator().next());

        underTest.close(firstClientSession, mock(CloseReason.class));
        verify(firstClientSession, times(2)).getRequestURI();
        verify(firstClientSession, times(2)).getId();
        verify(firstClientSession).close();
        verifyNoMoreInteractions(firstClientSession);
        assertEquals(0, underTest.getSessions().size());
    }

    /**
     * Makes sure a client session is properly closed and removed from the list of session when the
     * close method is invoked, even when that session throws an exception on close
     */
    @Test
    public void testClose_fromClientWithException() throws Exception {
        Session computeSession = mock(Session.class);
        URI computeURI = URI.create("http://fake" + RT_COMPUTE_ENDPOINT);
        when(computeSession.getRequestURI()).thenReturn(computeURI);
        underTest.onOpen(computeSession);
        assertEquals(0, underTest.getSessions().size());
        verify(computeSession).getRequestURI();
        verifyNoMoreInteractions(computeSession);
        assertTrue(underTest.isConnectedToCompute());

        // open first client session
        Session firstClientSession = mock(Session.class);
        URI resourceURI = URI.create("http://fake" + RT_EVENT_ENDPOINT);
        when(firstClientSession.getRequestURI()).thenReturn(resourceURI);
        underTest.onOpen(firstClientSession);
        verify(firstClientSession).getRequestURI();
        verify(firstClientSession).getId();
        verifyNoMoreInteractions(firstClientSession);
        assertEquals(1, underTest.getSessions().size());
        assertEquals(firstClientSession, underTest.getSessions().iterator().next());

        doThrow(IOException.class).when(firstClientSession).close();
        underTest.close(firstClientSession, mock(CloseReason.class));
        verify(firstClientSession, times(2)).getRequestURI();
        verify(firstClientSession, times(3)).getId();
        verify(firstClientSession).close();
        verifyNoMoreInteractions(firstClientSession);
        assertEquals(0, underTest.getSessions().size());
    }

    /**
     * Given an open and a closed session, this test makes sure that the event is only sent to the
     * open session. It also makes sure that the closed session gets removed from the list of open
     * sessions
     */
    @Test
    public void testOnMessage() {
        // open the compute connection
        Session computeSession = mock(Session.class);
        URI computeURI = URI.create("http://fake" + RT_COMPUTE_ENDPOINT);
        when(computeSession.getRequestURI()).thenReturn(computeURI);
        underTest.onOpen(computeSession);
        assertEquals(0, underTest.getSessions().size());
        verify(computeSession).getRequestURI();
        verifyNoMoreInteractions(computeSession);
        assertTrue(underTest.isConnectedToCompute());

        // add two sessions, one closed and one open
        Async asyncRemote = mock(Async.class);

        // open first client session
        Session firstClientSession = mock(Session.class);
        URI resourceURI = URI.create("http://fake" + RT_EVENT_ENDPOINT);
        when(firstClientSession.getRequestURI()).thenReturn(resourceURI);
        when(firstClientSession.isOpen()).thenReturn(true);
        when(firstClientSession.getAsyncRemote()).thenReturn(asyncRemote);
        underTest.onOpen(firstClientSession);
        verify(firstClientSession).getRequestURI();
        verify(firstClientSession).getId();
        verifyNoMoreInteractions(firstClientSession);
        assertEquals(1, underTest.getSessions().size());

        // open second client session
        Session secondClientSession = mock(Session.class);
        when(secondClientSession.getRequestURI()).thenReturn(resourceURI);
        when(secondClientSession.isOpen()).thenReturn(true);
        when(secondClientSession.getAsyncRemote()).thenReturn(asyncRemote);
        underTest.onOpen(secondClientSession);
        verify(secondClientSession).getRequestURI();
        verify(secondClientSession).getId();
        verifyNoMoreInteractions(secondClientSession);
        assertEquals(2, underTest.getSessions().size());

        // close the first session
        when(firstClientSession.isOpen()).thenReturn(false);
        ChatAlyticsEvent event = mock(ChatAlyticsEvent.class);
        underTest.onMessage(event);

        verify(event).setClazz(null);
        verify(firstClientSession, never()).getAsyncRemote();
        verify(secondClientSession).getAsyncRemote();
        verify(asyncRemote).sendObject(event);
        assertEquals(1, underTest.getSessions().size());
    }

    /**
     * Make sure that the exception doesn't propagate up
     */
    @Test
    public void testOnError() {
        underTest.onError(new RuntimeException());
    }
}
