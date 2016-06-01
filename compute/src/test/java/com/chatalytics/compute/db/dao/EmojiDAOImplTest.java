package com.chatalytics.compute.db.dao;

import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.EmojiEntity;
import com.google.common.base.Optional;

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

        mentionDate = DateTime.now(DateTimeZone.UTC);

        // Insert a bunch of test values
        underTest.persistEmoji(new EmojiEntity("emoji1", 1, mentionDate, "giannis", "room1"));
        underTest.persistEmoji(new EmojiEntity("emoji2", 1, mentionDate, "giannis", "room1"));
        underTest.persistEmoji(new EmojiEntity("emoji1", 1, mentionDate, "giannis", "room2"));
        underTest.persistEmoji(new EmojiEntity("emoji1", 1, mentionDate, "jane", "room1"));
    }

    @Test
    public void testPersistEmoji_withDuplicate() {
        EmojiEntity emoji = new EmojiEntity("test_value", 1, DateTime.now(), "user", "testroom");
        underTest.persistEmoji(emoji);
        EmojiEntity existingEmoji = underTest.getEmoji(emoji);
        assertNotNull(existingEmoji);
        assertEquals(1, existingEmoji.getOccurrences());

        // insert it again
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
                                                           Optional.absent(),
                                                           Optional.absent()));

        // make sure that the sums are correct for a bunch of different queries
        int result = underTest.getTotalMentionsForEmoji("emoji1", timeInterval,
                                                        Optional.absent(), Optional.absent());
        assertEquals(3, result);

        result = underTest.getTotalMentionsForEmoji("emoji1", timeInterval,
                                                    Optional.of("room1"), Optional.absent());
        assertEquals(2, result);

        result = underTest.getTotalMentionsForEmoji("emoji1", timeInterval,
                                                    Optional.of("room1"), Optional.of("giannis"));
        assertEquals(1, result);
    }

    /**
     * Makes sure that the correct amount of emoji mentions are returned
     */
    @Test
    public void testGetAllMentionsForEmoji() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        List<EmojiEntity> result = underTest.getAllMentionsForEmoji("emoji1", timeInterval,
                                                                   Optional.absent(),
                                                                   Optional.absent());
        assertEquals(3, result.size());

        result = underTest.getAllMentionsForEmoji("emoji1", timeInterval, Optional.of("room1"),
                                                  Optional.absent());
        assertEquals(2, result.size());

        result = underTest.getAllMentionsForEmoji("emoji1", timeInterval, Optional.of("room1"),
                                                  Optional.of("giannis"));
        assertEquals(1, result.size());
    }

    /**
     * Makes sure that the correct amount of all emoji mentions are returned
     */
    @Test
    public void testGetAllMentions() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        List<EmojiEntity> result = underTest.getAllMentions(timeInterval, Optional.absent(),
                                                            Optional.absent());
        assertEquals(4, result.size());

        result = underTest.getAllMentions(timeInterval, Optional.of("room1"), Optional.absent());
        assertEquals(3, result.size());

        result = underTest.getAllMentions(timeInterval, Optional.of("room1"),
                                          Optional.of("giannis"));
        assertEquals(2, result.size());
    }

    @Test
    public void testGetTopEmojis() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        Map<String, Long> result = underTest.getTopEmojis(timeInterval, Optional.absent(),
                                                          Optional.absent(), 10);
        assertEquals(2, result.size());
        assertEquals(3L, result.get("emoji1").longValue());
        assertEquals(1L, result.get("emoji2").longValue());

        result = underTest.getTopEmojis(timeInterval, Optional.of("room1"), Optional.absent(), 10);
        assertEquals(2, result.size());
        assertEquals(2L, result.get("emoji1").longValue());
        assertEquals(1L, result.get("emoji2").longValue());

        result = underTest.getTopEmojis(timeInterval, Optional.of("room1"), Optional.of("giannis"),
                                        10);
        assertEquals(2, result.size());
        assertEquals(1L, result.get("emoji1").longValue());
        assertEquals(1L, result.get("emoji2").longValue());
    }

    @Test
    public void testGetTopRoomsByToTV() {
        Interval interval = new Interval(mentionDate.minusMillis(1), mentionDate.plusMillis(1));

        Map<String, Double> result = underTest.getTopRoomsByEoTV(interval, 10);
        assertEquals(2, result.size());
        assertEquals(0.75, result.get("room1"), 0);
        assertEquals(0.25, result.get("room2"), 0);
    }

    @Test
    public void testGetTopUsersByToTV() {
        Interval interval = new Interval(mentionDate.minusMillis(1), mentionDate.plusMillis(1));

        Map<String, Double> result = underTest.getTopUsersByEoTV(interval, 10);
        assertEquals(2, result.size());
        assertEquals(0.75, result.get("giannis"), 0);
        assertEquals(0.25, result.get("jane"), 0);
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
