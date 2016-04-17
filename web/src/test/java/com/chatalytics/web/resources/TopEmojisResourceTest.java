package com.chatalytics.web.resources;

import com.chatalytics.compute.db.dao.EmojiDAOImpl;
import com.chatalytics.compute.db.dao.IEmojiDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.EmojiEntity;
import com.chatalytics.web.utils.DateTimeUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link EmojisResource}
 *
 * @author giannis
 *
 */
public class TopEmojisResourceTest {

    private IEmojiDAO entityDao;
    private EmojisResource undertest;
    private DateTimeZone dtZone;
    private DateTime mentionTime;

    @Before
    public void setUp() throws Exception {
        ChatAlyticsConfig config = new ChatAlyticsConfig();
        config.persistenceUnitName = "chatalytics-web-test";
        config.timeZone = "America/New_York";
        dtZone = DateTimeZone.forID(config.timeZone);

        entityDao = new EmojiDAOImpl(config);
        entityDao.startAsync().awaitRunning();

        mentionTime = DateTime.now().withZone(DateTimeZone.UTC);
        List<EmojiEntity> emojis = Lists.newArrayListWithCapacity(10);
        emojis.add(new EmojiEntity("e1", 5, mentionTime.minusHours(1), "u1", "r1"));
        emojis.add(new EmojiEntity("e1", 4, mentionTime.minusHours(1), "u2", "r1"));
        emojis.add(new EmojiEntity("e1", 1, mentionTime.minusHours(2), "u3", "r2"));
        emojis.add(new EmojiEntity("e1", 1, mentionTime.minusHours(3), "u4", "r3"));
        emojis.add(new EmojiEntity("e2", 1, mentionTime.minusHours(1), "u1", "r1"));
        emojis.add(new EmojiEntity("e2", 6, mentionTime.minusHours(1), "u2", "r1"));
        emojis.add(new EmojiEntity("e2", 7, mentionTime.minusHours(2), "u3", "r2"));
        emojis.add(new EmojiEntity("e3", 3, mentionTime.minusHours(1), "u2", "r2"));
        emojis.add(new EmojiEntity("e3", 3, mentionTime.minusHours(2), "u3", "r1"));
        emojis.add(new EmojiEntity("e4", 3, mentionTime.minusHours(1), "u1", "r4"));
        storeTestEmojis(emojis);
        undertest = new EmojisResource(config);
    }

    private void storeTestEmojis(List<EmojiEntity> emojis) {
        for (EmojiEntity emoji : emojis) {
            entityDao.persistEmoji(emoji);
        }
    }

    /**
     * Tests to see if the correct top emojis are returned
     */
    @Test
    public void testGetTopEmojis() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Long> response = undertest.getTopEmojis(startTimeStr, endTimeStr, "u1", "r1",
                                                            null);
        Map<String, Long> expected = Maps.newHashMap();
        expected.put("e1", 5L);
        expected.put("e2", 1L);
        assertEquals(expected, response);

        response = undertest.getTopEmojis(startTimeStr, endTimeStr, "u1", null, null);
        expected.clear();
        expected.put("e1", 5L);
        expected.put("e4", 3L);
        expected.put("e2", 1L);
        assertEquals(expected, response);

        response = undertest.getTopEmojis(startTimeStr, endTimeStr, null, null, null);
        expected.clear();
        expected.put("e2", 14L);
        expected.put("e1", 11L);
        expected.put("e3", 6L);
        expected.put("e4", 3L);
        assertEquals(expected, response);
    }

    @After
    public void tearDown() throws Exception {
        entityDao.stopAsync().awaitTerminated();
    }
}
