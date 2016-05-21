package com.chatalytics.compute.db.dao;

import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.MessageSummary;
import com.chatalytics.core.model.MessageType;
import com.google.common.base.Optional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link MessageSummaryDAOImpl}
 *
 * @author giannis
 */
public class MessageSummaryDAOImplTest {

    private IMessageSummaryDAO underTest;
    private DateTime mentionDate;
    private ChatAlyticsConfig config;

    @Before
    public void setUp() throws Exception {
        config = new ChatAlyticsConfig();
        config.persistenceUnitName = "chatalytics-db-test";
        underTest = ChatAlyticsDAOFactory.createMessageSummaryDAO(config);
        underTest.startAsync().awaitRunning();

        mentionDate = new DateTime().withZone(DateTimeZone.UTC);

        // Insert a bunch of test values
        underTest.persistMessageSummary(new MessageSummary("user1", "room1", mentionDate,
                                                           MessageType.MESSAGE, 1));
        underTest.persistMessageSummary(new MessageSummary("user2", "room1", mentionDate,
                                                           MessageType.MESSAGE, 1));
        underTest.persistMessageSummary(new MessageSummary("user3", "room2", mentionDate,
                                                           MessageType.PINNED_ITEM, 1));
        underTest.persistMessageSummary(new MessageSummary("user4", "room2", mentionDate,
                                                           MessageType.CHANNEL_JOIN, 1));
    }

    @Test
    public void testGetMentionSummary() {
        MessageSummary msgSummary = new MessageSummary("user1", "room1", mentionDate,
                                                       MessageType.MESSAGE, 0);
        MessageSummary result = underTest.getMessageSummary(msgSummary);
        assertNotNull(result);
        assertEquals(msgSummary.getMentionTime(), result.getMentionTime());
        assertEquals(1, result.getOccurrences());
        assertEquals(msgSummary.getRoomName(), result.getRoomName());
        assertEquals(msgSummary.getUsername(), result.getUsername());
        assertEquals(msgSummary.getValue(), result.getValue());
    }

    @Test
    public void testPersistValue_withDuplicate() {
        MessageSummary msgSummary = new MessageSummary("test_user", "room2", mentionDate,
                                                       MessageType.PINNED_ITEM, 1);
        underTest.persistMessageSummary(msgSummary);
        MessageSummary existingMsgSummary = underTest.getMessageSummary(msgSummary);
        assertNotNull(existingMsgSummary);
        assertEquals(1, existingMsgSummary.getOccurrences());

        // insert it again
        underTest.persistMessageSummary(msgSummary);
        existingMsgSummary = underTest.getMessageSummary(msgSummary);
        assertEquals(2, existingMsgSummary.getOccurrences());
    }

    @Test
    public void testGetTotalMessageSummaries() {
        Interval interval = new Interval(mentionDate.minusDays(1),  mentionDate.plusDays(1));
        int result = underTest.getTotalMessageSummaries(interval, Optional.absent(),
                                                        Optional.absent());
        assertEquals(4, result);
    }

    @Test
    public void testGetTotalMessageSummaries_withValue() {
        Interval interval = new Interval(mentionDate.minusDays(1),  mentionDate.plusDays(1));
        int result = underTest.getTotalMessageSummaries(MessageType.MESSAGE,
                                                        interval, Optional.absent(),
                                                        Optional.absent());
        assertEquals(2, result);
    }

    @After
    public void tearDown() {
        EntityManager em = ChatAlyticsDAOFactory.getEntityManagerFactory(config)
                                                .createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM " + MessageSummary.MESSAGE_SUMMARY_TABLE_NAME)
          .executeUpdate();
        em.getTransaction().commit();
        underTest.stopAsync().awaitTerminated();
    }

}
