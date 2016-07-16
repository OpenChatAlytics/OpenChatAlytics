package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.EmojiEntity;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.model.data.MessageType;
import com.google.common.collect.ImmutableList;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests {@link IEmojiDAO}
 *
 * @author giannis
 *
 */
public class EmojiDAOImplTest {

    private IEmojiDAO underTest;
    private DateTime mentionDate;
    private ChatAlyticsConfig config;

    @Before
    public void setUp() throws Exception {
        config = new ChatAlyticsConfig();
        config.persistenceUnitName = "chatalytics-db-test";
        underTest = ChatAlyticsDAOFactory.createEmojiDAO(config);
        underTest.startAsync().awaitRunning();
        IMessageSummaryDAO msgSummaryDao = ChatAlyticsDAOFactory.createMessageSummaryDAO(config);
        msgSummaryDao.startAsync().awaitRunning();
        mentionDate = DateTime.now(DateTimeZone.UTC);

        // Insert a bunch of test values
        underTest.persistEmoji(new EmojiEntity("giannis", "room1", mentionDate, "e1", 1, false));
        underTest.persistEmoji(new EmojiEntity("giannis", "room1", mentionDate, "e2", 1, false));
        underTest.persistEmoji(new EmojiEntity("giannis", "room2", mentionDate, "e1", 1, false));
        underTest.persistEmoji(new EmojiEntity("jane", "room1", mentionDate, "e1", 1, false));

        msgSummaryDao.persistMessageSummary(new MessageSummary("giannis", "room1", mentionDate,
                                                               MessageType.MESSAGE, 10, false));
        msgSummaryDao.persistMessageSummary(new MessageSummary("jane", "room1", mentionDate,
                                                               MessageType.MESSAGE, 10, false));
        msgSummaryDao.stopAsync().awaitTerminated();
    }

    @Test
    public void testPersistEmoji_withDuplicate() {
        EmojiEntity emoji = new EmojiEntity("user", "testroom", mentionDate, "test", 1, false);
        underTest.persistEmoji(emoji);
        EmojiEntity existingEmoji = underTest.getEmoji(emoji);
        assertNotNull(existingEmoji);
        assertEquals(1, existingEmoji.getOccurrences());

        // insert it again
        emoji = new EmojiEntity("user", "testroom", mentionDate, "test", 1, false);
        underTest.persistEmoji(emoji);
        existingEmoji = underTest.getEmoji(emoji);
        assertEquals(2, existingEmoji.getOccurrences());
    }

    /**
     * Makes sure that the correct emoji occurrence sums are returned.
     */
    @Test
    public void testGetTotalMentionsForEmoji() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));

        // Make sure that 0 is returned when nothing is found
        assertEquals(0, underTest.getTotalMentionsForEmoji("unknownemoji",
                                                           timeInterval,
                                                           ImmutableList.of(),
                                                           ImmutableList.of()));

        // make sure that the sums are correct for a bunch of different queries
        int result = underTest.getTotalMentionsForEmoji("e1", timeInterval,
                                                        ImmutableList.of(), ImmutableList.of());
        assertEquals(3, result);

        result = underTest.getTotalMentionsForEmoji("e1", timeInterval,
                                                    ImmutableList.of("room1"), ImmutableList.of());
        assertEquals(2, result);

        result = underTest.getTotalMentionsForEmoji("e1", timeInterval,
                                                    ImmutableList.of("room1"),
                                                    ImmutableList.of("giannis"));
        assertEquals(1, result);
    }

    /**
     * Makes sure that the correct amount of emoji mentions are returned
     */
    @Test
    public void testGetAllMentionsForEmoji() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        List<EmojiEntity> result = underTest.getAllMentionsForEmoji("e1", timeInterval,
                                                                   ImmutableList.of(),
                                                                   ImmutableList.of());
        assertEquals(3, result.size());

        result = underTest.getAllMentionsForEmoji("e1", timeInterval, ImmutableList.of("room1"),
                                                  ImmutableList.of());
        assertEquals(2, result.size());

        result = underTest.getAllMentionsForEmoji("e1", timeInterval, ImmutableList.of("room1"),
                                                  ImmutableList.of("giannis"));
        assertEquals(1, result.size());
    }

    /**
     * Makes sure that the correct amount of all emoji mentions are returned
     */
    @Test
    public void testGetAllMentions() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        List<EmojiEntity> result = underTest.getAllMentions(timeInterval, ImmutableList.of(),
                                                            ImmutableList.of());
        assertEquals(4, result.size());

        result = underTest.getAllMentions(timeInterval, ImmutableList.of("room1"),
                                          ImmutableList.of());
        assertEquals(3, result.size());

        result = underTest.getAllMentions(timeInterval, ImmutableList.of("room1"),
                                          ImmutableList.of("giannis"));
        assertEquals(2, result.size());
    }

    @Test
    public void testGetTopEmojis() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        Map<String, Long> result = underTest.getTopEmojis(timeInterval, ImmutableList.of(),
                                                          ImmutableList.of(), 10);
        assertEquals(2, result.size());
        assertEquals(3L, result.get("e1").longValue());
        assertEquals(1L, result.get("e2").longValue());

        result = underTest.getTopEmojis(timeInterval, ImmutableList.of("room1"), ImmutableList.of(),
                                        10);
        assertEquals(2, result.size());
        assertEquals(2L, result.get("e1").longValue());
        assertEquals(1L, result.get("e2").longValue());

        result = underTest.getTopEmojis(timeInterval, ImmutableList.of("room1"),
                                        ImmutableList.of("giannis"), 10);
        assertEquals(2, result.size());
        assertEquals(1L, result.get("e1").longValue());
        assertEquals(1L, result.get("e2").longValue());
    }

    @Test
    public void testGetRoomSimilaritiesByEmoji() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        LabeledDenseMatrix<String> result = underTest.getRoomSimilaritiesByEmoji(timeInterval);
        assertEquals(2, result.getLabels().size());
        assertEquals(2, result.getMatrix().length);
    }

    @Test
    public void testGetUserSimilaritiesByEmoji() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        LabeledDenseMatrix<String> result = underTest.getUserSimilaritiesByEmoji(timeInterval);
        assertEquals(2, result.getLabels().size());
        assertEquals(2, result.getMatrix().length);
    }

    @Test
    public void testGetActiveRoomsByMethod() {
        Interval interval = new Interval(mentionDate.minusMillis(1), mentionDate.plusMillis(1));

        Map<String, Double> result = underTest.getActiveRoomsByMethod(interval, ActiveMethod.ToTV,
                                                                      10);
        assertEquals(2, result.size());
        assertEquals(0.75, result.get("room1"), 0);
        assertEquals(0.25, result.get("room2"), 0);

        result = underTest.getActiveRoomsByMethod(interval, ActiveMethod.ToMV, 10);
        assertEquals(2, result.size());
        assertEquals(0.15, result.get("room1"), 0);
        assertEquals(0.05, result.get("room2"), 0);
    }

    @Test
    public void testGetActiveUsersByMethod() {
        Interval interval = new Interval(mentionDate.minusMillis(1), mentionDate.plusMillis(1));

        Map<String, Double> result =
                underTest.getActiveUsersByMethod(interval, ActiveMethod.ToTV, 10);
        assertEquals(2, result.size());
        assertEquals(0.75, result.get("giannis"), 0);
        assertEquals(0.25, result.get("jane"), 0);

        result = underTest.getActiveUsersByMethod(interval, ActiveMethod.ToMV, 10);
        assertEquals(2, result.size());
        assertEquals(0.15, result.get("giannis"), 0);
        assertEquals(0.05, result.get("jane"), 0);
    }

    @After
    public void tearDown() throws Exception {
        EntityManager em = ChatAlyticsDAOFactory.getEntityManagerFactory(config)
                                                .createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM " + EmojiEntity.EMOJI_TABLE_NAME).executeUpdate();
        em.getTransaction().commit();
        underTest.stopAsync().awaitTerminated();
    }

}
