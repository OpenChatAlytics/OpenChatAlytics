package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.EmojiEntity;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.model.data.MessageType;
import com.google.common.base.Optional;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link MentionableDAO}
 *
 * @author giannis
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class MentionableDAOTest {

    private MentionableDAO<String, EmojiEntity> underTest;
    private EntityManagerFactory entityManagerFactory;
    private ChatAlyticsConfig config;

    @Before
    public void setUp() {
        config = new ChatAlyticsConfig();
        config.persistenceUnitName = "chatalytics-db-test";
        entityManagerFactory = ChatAlyticsDAOFactory.getEntityManagerFactory(config);
        underTest = new MentionableDAO<>(entityManagerFactory, EmojiEntity.class);
    }

    @Test
    public void testGetRoomSimilarities() throws Exception {
        DateTime end = DateTime.now();
        DateTime start = end.minusDays(1);

        // make r1, r2 and r3 kind of similar and r4
        underTest.persistValue(new EmojiEntity("a", 1, start, "u1", "r1"));
        underTest.persistValue(new EmojiEntity("a", 1, start.plusMillis(1), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("a", 1, start.plusMillis(2), "u1", "r3"));
        underTest.persistValue(new EmojiEntity("b", 1, start.plusMillis(3), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("b", 1, start.plusMillis(4), "u1", "r3"));
        underTest.persistValue(new EmojiEntity("c", 1, start.plusMillis(5), "u1", "r1"));
        underTest.persistValue(new EmojiEntity("c", 1, start.plusMillis(6), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("d", 1, start.plusMillis(7), "u1", "r1"));
        underTest.persistValue(new EmojiEntity("d", 1, start.plusMillis(8), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("d", 1, start.plusMillis(9), "u1", "r3"));

        underTest.persistValue(new EmojiEntity("e", 1, start.plusMillis(10), "u1", "r4"));
        underTest.persistValue(new EmojiEntity("e", 1, start.plusMillis(11), "u1", "r4"));
        underTest.persistValue(new EmojiEntity("e", 1, start.plusMillis(12), "u1", "r5"));
        underTest.persistValue(new EmojiEntity("f", 1, start.plusMillis(13), "u1", "r6"));
        underTest.persistValue(new EmojiEntity("g", 1, start.plusMillis(14), "u1", "r7"));
        underTest.persistValue(new EmojiEntity("h", 1, start.plusMillis(15), "u1", "r7"));

        Interval interval = new Interval(start, end);
        LabeledDenseMatrix<String> result = underTest.getRoomSimilaritiesByValue(interval);
        assertEquals(7, result.getMatrix().length);
        assertEquals(7, result.getLabels().size());
    }

    @Test
    public void testPersistValue() {
        DateTime dateTime = DateTime.now();
        EmojiEntity emoji = new EmojiEntity("a", 1, dateTime, "u1", "r1");
        assertNull(underTest.getValue(emoji));
        underTest.persistValue(emoji);
        assertNotNull(underTest.getValue(emoji));
    }

    @Test(expected = EntityExistsException.class)
    public void testPersistValue_withDuplicate() {
        DateTime dateTime = DateTime.now();
        EmojiEntity emoji = new EmojiEntity("a", 1, dateTime, "u1", "r1");
        assertNull(underTest.getValue(emoji));
        underTest.persistValue(emoji);
        assertNotNull(underTest.getValue(emoji));

        // store it again and make sure an exception is not thrown
        underTest.persistValue(emoji);
    }

    @Test
    public void testGetTotalMentionsOfType() {
        DateTime end = DateTime.now();
        DateTime start = end.minusDays(1);
        Interval interval = new Interval(start, end);
        underTest.persistValue(new EmojiEntity("a", 1, start, "u1", "r1"));
        underTest.persistValue(new EmojiEntity("a", 1, start.plusMillis(1), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("a", 1, start.plusMillis(2), "u2", "r1"));
        underTest.persistValue(new EmojiEntity("b", 1, start.plusMillis(3), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("b", 1, start.plusMillis(4), "u2", "r3"));
        underTest.persistValue(new EmojiEntity("c", 1, start.plusMillis(5), "u1", "r3"));

        int result = underTest.getTotalMentionsOfType(interval, Optional.absent(),
                                                      Optional.absent());
        assertEquals(6, result);

        result = underTest.getTotalMentionsOfType(interval, Optional.of("r1"), Optional.absent());
        assertEquals(2, result);

        result = underTest.getTotalMentionsOfType(interval, Optional.absent(), Optional.of("u1"));
        assertEquals(4, result);

        result = underTest.getTotalMentionsOfType(interval, Optional.of("r1"), Optional.of("u1"));
        assertEquals(1, result);
    }

    @Test
    public void testGetTotalMentionsForType() {
        DateTime end = DateTime.now();
        DateTime start = end.minusDays(1);
        Interval interval = new Interval(start, end);
        underTest.persistValue(new EmojiEntity("a", 1, start, "u1", "r1"));
        underTest.persistValue(new EmojiEntity("a", 1, start.plusMillis(1), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("a", 1, start.plusMillis(2), "u2", "r3"));
        underTest.persistValue(new EmojiEntity("b", 1, start.plusMillis(3), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("c", 1, start.plusMillis(5), "u1", "r3"));

        int result = underTest.getTotalMentionsForType("a", interval, Optional.absent(),
                                                       Optional.absent());
        assertEquals(3, result);

        result = underTest.getTotalMentionsForType("a", interval, Optional.of("r1"),
                                                   Optional.absent());
        assertEquals(1, result);

        result = underTest.getTotalMentionsForType("a", interval, Optional.absent(),
                                                   Optional.of("u1"));
        assertEquals(2, result);
    }

    @Test
    public void testGetTopColumnsByToTV() throws Exception {
        DateTime end = DateTime.now();
        DateTime start = end.minusDays(1);

        underTest.persistValue(new EmojiEntity("a", 1, start.plusMillis(1), "u1", "r1"));
        underTest.persistValue(new EmojiEntity("b", 1, start.plusMillis(2), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("c", 1, start.plusMillis(3), "u2", "r3"));
        underTest.persistValue(new EmojiEntity("d", 1, start.plusMillis(4), "u1", "r4"));
        underTest.persistValue(new EmojiEntity("e", 1, start.plusMillis(5), "u1", "r4"));
        underTest.persistValue(new EmojiEntity("f", 1, start.plusMillis(6), "u1", "r5"));
        underTest.persistValue(new EmojiEntity("g", 1, start.plusMillis(7), "u1", "r5"));
        underTest.persistValue(new EmojiEntity("h", 1, start.plusMillis(8), "u1", "r5"));

        Interval interval = new Interval(start, end);

        Map<String, Double> result = underTest.getTopColumnsByToTV("roomName", interval, 100);
        assertEquals(5, result.size());
        assertEquals(0.375, result.get("r5").doubleValue(), 0);
        assertEquals(0.25, result.get("r4").doubleValue(), 0);
        assertEquals(0.125, result.get("r3").doubleValue(), 0);
        assertEquals(0.125, result.get("r2").doubleValue(), 0);
        assertEquals(0.125, result.get("r1").doubleValue(), 0);

        // check that they're in descending order
        double previousValue = Double.MIN_VALUE;
        for (double value : result.values()) {
            assertTrue(Double.compare(value, previousValue) > 0);
        }

        // check with a smaller interval
        interval = new Interval(start.plusMillis(2), start.plusMillis(6));
        result = underTest.getTopColumnsByToTV("roomName", interval, 100);
        assertEquals(3, result.size());
        Map.Entry<String, Double> firstEntry = result.entrySet().iterator().next();
        assertEquals("r4", firstEntry.getKey());
        assertEquals(0.5, firstEntry.getValue().doubleValue(), 0);
    }

    @Test
    public void testGetTopColumnsByToMV() throws Exception {
        DateTime end = DateTime.now();
        DateTime start = end.minusDays(1);
        Interval interval = new Interval(start, end);

        IMessageSummaryDAO msgSummaryDao =  ChatAlyticsDAOFactory.createMessageSummaryDAO(config);
        msgSummaryDao.startAsync().awaitRunning();

        underTest.persistValue(new EmojiEntity("a", 1, start.plusMillis(1), "u1", "r1"));
        underTest.persistValue(new EmojiEntity("c", 1, start.plusMillis(3), "u2", "r1"));
        underTest.persistValue(new EmojiEntity("a", 1, start.plusMillis(4), "u1", "r2"));

        msgSummaryDao.persistMessageSummary(new MessageSummary("u1", "r1", start.plusMillis(1),
                                                               MessageType.MESSAGE, 1));
        msgSummaryDao.persistMessageSummary(new MessageSummary("u1", "r1", start.plusMillis(1),
                                                               MessageType.MESSAGE, 1));
        msgSummaryDao.persistMessageSummary(new MessageSummary("u1", "r1", start.plusMillis(1),
                                                               MessageType.CHANNEL_JOIN, 1));
        msgSummaryDao.persistMessageSummary(new MessageSummary("u2", "r1", start.plusMillis(1),
                                                               MessageType.MESSAGE, 1));
        msgSummaryDao.persistMessageSummary(new MessageSummary("u2", "r1", start.plusMillis(1),
                                                               MessageType.CHANNEL_JOIN, 1));
        msgSummaryDao.persistMessageSummary(new MessageSummary("u1", "r2", start.plusMillis(1),
                                                               MessageType.MESSAGE, 1));
        msgSummaryDao.persistMessageSummary(new MessageSummary("u3", "r2", start.plusMillis(1),
                                                               MessageType.MESSAGE, 1));
        msgSummaryDao.persistMessageSummary(new MessageSummary("u1", "r2", start.plusMillis(1),
                                                               MessageType.MESSAGE_CHANGED, 1));

        Map<String, Double> result = underTest.getTopColumnsByToMV("username", interval, 100);
        assertEquals(2, result.size());
        // u1 has 3 messages and 2 emojis. u2 has 2 messages and 1 emoji. Total message volume is 5
        // u1 should be 2/5 and u2 should be 1/5
        assertEquals(0.4, result.get("u1").doubleValue(), 0);
        assertEquals(0.2, result.get("u2").doubleValue(), 0);

        // check that they're in descending order
        double previousValue = Double.MIN_VALUE;
        for (double value : result.values()) {
            assertTrue(Double.compare(value, previousValue) > 0);
        }

        // check with a smaller interval
        interval = new Interval(start.plusMillis(2), start.plusMillis(6));
        result = underTest.getTopColumnsByToTV("username", interval, 100);
        assertEquals(2, result.size());
        assertEquals(0.5, result.get("u1").doubleValue(), 0);
        assertEquals(0.5, result.get("u2").doubleValue(), 0);
        // check that they're in descending order
        previousValue = Double.MIN_VALUE;
        for (double value : result.values()) {
            assertTrue(Double.compare(value, previousValue) > 0);
        }

        msgSummaryDao.stopAsync().awaitTerminated();
    }

    @After
    public void tearDown() {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM " + EmojiEntity.EMOJI_TABLE_NAME).executeUpdate();
        em.createNativeQuery("DELETE FROM " + MessageSummary.MESSAGE_SUMMARY_TABLE_NAME)
          .executeUpdate();
        em.getTransaction().commit();
        underTest.close();
    }

}
