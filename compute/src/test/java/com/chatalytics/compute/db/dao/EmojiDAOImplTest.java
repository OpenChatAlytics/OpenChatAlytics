package com.chatalytics.compute.db.dao;

import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.EmojiEntity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;

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
