package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.EmojiEntity;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManagerFactory;

import static org.junit.Assert.assertEquals;

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

    @Before
    public void setUp() {
        ChatAlyticsConfig config = new ChatAlyticsConfig();
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
        underTest.persistValue(new EmojiEntity("a", 1, start.plusMillis(1), "u1", "r3"));
        underTest.persistValue(new EmojiEntity("b", 1, start.plusMillis(1), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("b", 1, start.plusMillis(1), "u1", "r3"));
        underTest.persistValue(new EmojiEntity("c", 1, start.plusMillis(1), "u1", "r1"));
        underTest.persistValue(new EmojiEntity("c", 1, start.plusMillis(1), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("d", 1, start.plusMillis(1), "u1", "r1"));
        underTest.persistValue(new EmojiEntity("d", 1, start.plusMillis(1), "u1", "r2"));
        underTest.persistValue(new EmojiEntity("d", 1, start.plusMillis(1), "u1", "r3"));

        underTest.persistValue(new EmojiEntity("e", 1, start.plusMillis(1), "u1", "r4"));
        underTest.persistValue(new EmojiEntity("e", 1, start.plusMillis(1), "u1", "r4"));
        underTest.persistValue(new EmojiEntity("e", 1, start.plusMillis(1), "u1", "r5"));
        underTest.persistValue(new EmojiEntity("f", 1, start.plusMillis(1), "u1", "r6"));
        underTest.persistValue(new EmojiEntity("g", 1, start.plusMillis(1), "u1", "r7"));
        underTest.persistValue(new EmojiEntity("h", 1, start.plusMillis(1), "u1", "r7"));

        Interval interval = new Interval(start, end);
        LabeledDenseMatrix<String> result = underTest.getRoomSimilaritiesByValue(interval);
        assertEquals(7, result.getMatrix().length);
        assertEquals(7, result.getLabels().size());
    }
}
