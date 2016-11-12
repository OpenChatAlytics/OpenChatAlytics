package com.chatalytics.compute.web.realtime;

import com.chatalytics.core.model.data.ChatAlyticsEvent;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.realtime.ConnectionType;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link RealtimeResource}
 *
 * @author giannis
 *
 */
public class RealtimeResourceTest {

    private Session session;
    private RealtimeResource underTest;

    @Before
    public void setUp() {
        session = mock(Session.class);
        when(session.getId()).thenReturn("id");
        when(session.isOpen()).thenReturn(true);
        underTest = new RealtimeResource();
    }

    @Test
    public void testOpenSocket_withPublisherSession() {
        ConnectionType type = ConnectionType.PUBLISHER;

        assertEquals(0, underTest.numSessions());
        underTest.openSocket(type, session);
        verify(session).getId();
        verify(session).setMaxIdleTimeout(0);
        verifyNoMoreInteractions(session);
        assertEquals(0, underTest.numSessions());
    }

    @Test
    public void testOpenSocket_withSubscriberSession() {
        ConnectionType type = ConnectionType.SUBSCRIBER;

        assertEquals(0, underTest.numSessions());
        underTest.openSocket(type, session);
        verify(session).getId();
        verify(session).setMaxIdleTimeout(0);
        verifyNoMoreInteractions(session);
        assertEquals(1, underTest.numSessions());

        // add a second session
        Session secondSession = mock(Session.class);
        when(secondSession.getId()).thenReturn("id2");
        when(secondSession.isOpen()).thenReturn(true);
        underTest.openSocket(type, secondSession);
        verify(session).isOpen();
        verifyNoMoreInteractions(session);
        verify(secondSession).getId();
        verify(secondSession).setMaxIdleTimeout(0);
        verifyNoMoreInteractions(secondSession);
        assertEquals(2, underTest.numSessions());

        // add a third session and close the second one to make sure that it is removed
        when(secondSession.isOpen()).thenReturn(false);
        Session thirdSession = mock(Session.class);
        when(thirdSession.getId()).thenReturn("id3");
        when(thirdSession.isOpen()).thenReturn(true);
        underTest.openSocket(type, thirdSession);
        verify(session, times(2)).isOpen();
        verifyNoMoreInteractions(session);
        verify(secondSession).isOpen();
        verifyNoMoreInteractions(secondSession);
        verify(thirdSession).getId();
        verify(thirdSession).setMaxIdleTimeout(0);
        assertEquals(2, underTest.numSessions());
    }

    /**
     * Creates two sessions one that's closed and one that's open, sends an event and makes sure
     * that the closed gets collected and removed and that the event only gets propagated to the
     * open one
     */
    @Test
    public void testPublishEvent() {
        MessageSummary actualEvent = mock(MessageSummary.class);
        String eventType = actualEvent.getClass().getSimpleName();
        ChatAlyticsEvent event = new ChatAlyticsEvent(DateTime.now(), eventType, actualEvent);

        Async asyncRemote = mock(Async.class);
        when(session.getAsyncRemote()).thenReturn(asyncRemote);
        // open two sockets make one open and one closed
        ConnectionType type = ConnectionType.SUBSCRIBER;
        underTest.openSocket(type, session);
        verify(session).getId();
        verify(session).setMaxIdleTimeout(0);
        verifyNoMoreInteractions(session);
        Session closedSession = mock(Session.class);
        when(closedSession.getId()).thenReturn("id2");
        when(closedSession.isOpen()).thenReturn(false);
        underTest.openSocket(type, closedSession);
        verify(closedSession).getId();
        verify(closedSession).setMaxIdleTimeout(0);
        verifyNoMoreInteractions(closedSession);
        verify(session).isOpen();
        verifyNoMoreInteractions(session);
        assertEquals(2, underTest.numSessions());

        underTest.publishEvent(event);
        verify(session, times(2)).isOpen();
        verify(session).getAsyncRemote();
        verifyNoMoreInteractions(session);
        verify(asyncRemote).sendObject(event);
        verifyNoMoreInteractions(asyncRemote);
        verify(closedSession).isOpen();
        verifyNoMoreInteractions(closedSession);
        assertEquals(1, underTest.numSessions());
    }

    @Test
    public void testClose() throws Exception {
        underTest.openSocket(ConnectionType.SUBSCRIBER, session);
        verify(session).getId();
        verify(session).setMaxIdleTimeout(0);
        verifyNoMoreInteractions(session);
        assertEquals(1, underTest.numSessions());
        underTest.close(session, new CloseReason(CloseCodes.CANNOT_ACCEPT, "close"));
        verify(session).close();
        verify(session, times(2)).getId();
        verifyNoMoreInteractions(session);
        assertEquals(0, underTest.numSessions());
    }

    @Test
    public void testClose_withException() throws Exception {
        underTest.openSocket(ConnectionType.SUBSCRIBER, session);
        verify(session).getId();
        verify(session).setMaxIdleTimeout(0);
        verifyNoMoreInteractions(session);
        assertEquals(1, underTest.numSessions());

        // make close throw an exception
        doThrow(IOException.class).when(session).close();
        underTest.close(session, new CloseReason(CloseCodes.CANNOT_ACCEPT, "close"));
        verify(session).close();
        verify(session, times(3)).getId();
        verifyNoMoreInteractions(session);
        assertEquals(0, underTest.numSessions());
    }

    /**
     * At the time of this unit test was written {@link RealtimeResource#onError(Throwable)} simply
     * logged the exception
     */
    @Test
    public void testOnError() {
        underTest.onError(new RuntimeException("log"));
    }
}
