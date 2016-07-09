package com.chatalytics.web.resources;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IEmojiDAO;
import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.DimensionType;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.emoji.LocalEmojiUtils;
import com.chatalytics.core.json.JsonObjectMapperFactory;
import com.chatalytics.core.model.data.EmojiEntity;
import com.chatalytics.core.model.data.EmojiMap;
import com.chatalytics.web.utils.DateTimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link EmojisResource}
 *
 * @author giannis
 *
 */
public class EmojisResourceTest {

    private IEmojiDAO entityDao;
    private EmojisResource underTest;
    private DateTimeZone dtZone;
    private DateTime mentionTime;
    private List<EmojiEntity> emojis;
    private ChatAlyticsConfig config;
    private IChatApiDAO chatApiDao;

    @Before
    public void setUp() throws Exception {
        config = new ChatAlyticsConfig();
        config.persistenceUnitName = "chatalytics-web-test";
        config.timeZone = "America/New_York";
        dtZone = DateTimeZone.forID(config.timeZone);

        entityDao = ChatAlyticsDAOFactory.createEmojiDAO(config);
        entityDao.startAsync().awaitRunning();

        mentionTime = DateTime.now().withZone(DateTimeZone.UTC);
        emojis = Lists.newArrayListWithCapacity(10);
        emojis.add(new EmojiEntity("u1", "r1", mentionTime.minusHours(1), "e1", 5));
        emojis.add(new EmojiEntity("u2", "r1", mentionTime.minusHours(1), "e1", 4));
        emojis.add(new EmojiEntity("u3", "r2", mentionTime.minusHours(2), "e1", 1));
        emojis.add(new EmojiEntity("u4", "r3", mentionTime.minusHours(3), "e1", 1));
        emojis.add(new EmojiEntity("u1", "r1", mentionTime.minusHours(1), "e2", 1));
        emojis.add(new EmojiEntity("u2", "r1", mentionTime.minusHours(1), "e2", 6));
        emojis.add(new EmojiEntity("u3", "r2", mentionTime.minusHours(2), "e2", 7));
        emojis.add(new EmojiEntity("u2", "r2", mentionTime.minusHours(1), "e3", 3));
        emojis.add(new EmojiEntity("u3", "r1", mentionTime.minusHours(2), "e3", 3));
        emojis.add(new EmojiEntity("u1", "r4", mentionTime.minusHours(1), "e4", 3));

        storeTestEmojis(emojis);
        chatApiDao = mock(IChatApiDAO.class);
        underTest = new EmojisResource(config, chatApiDao);
    }

    /**
     * Tests to see if the correct top emojis are returned
     */
    @Test
    public void testGetTopEmojis() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Long> response = underTest.getTopEmojis(startTimeStr, endTimeStr,
                                                            ImmutableList.of("u1"),
                                                            ImmutableList.of("r1"),
                                                            null);
        Map<String, Long> expected = Maps.newHashMap();
        expected.put("e1", 5L);
        expected.put("e2", 1L);
        assertEquals(expected, response);

        response = underTest.getTopEmojis(startTimeStr, endTimeStr, ImmutableList.of("u1"),
                                          null, null);
        expected.clear();
        expected.put("e1", 5L);
        expected.put("e4", 3L);
        expected.put("e2", 1L);
        assertEquals(expected, response);

        response = underTest.getTopEmojis(startTimeStr, endTimeStr, null, null, null);
        expected.clear();
        expected.put("e2", 14L);
        expected.put("e1", 11L);
        expected.put("e3", 6L);
        expected.put("e4", 3L);
        assertEquals(expected, response);
    }

    @Test
    public void testGetAllEmojis() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        List<EmojiEntity> result = underTest.getAllEmojis(startTimeStr, endTimeStr, null, null);
        assertEquals(emojis.size(), result.size());

        Set<EmojiEntity> resultEmojiSet = Sets.newHashSet(result);
        for (EmojiEntity expectedEmoji : emojis) {
            assertTrue(resultEmojiSet.contains(expectedEmoji));
        }
    }

    /**
     * Tests the similarities endpoint
     */
    @Test
    public void testGetSimilarities_RoomByEmoji() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));

        LabeledDenseMatrix<String> ldm = underTest.getSimilarities(
                startTimeStr, endTimeStr, DimensionType.ROOM.getDimensionName(),
                DimensionType.EMOJI.getDimensionName());
        assertEquals(4, ldm.getLabels().size());
        assertEquals(4, ldm.getMatrix().length);
    }

    /**
     * Tests the similarities endpoint
     */
    @Test
    public void testGetSimilarities_UserByEmoji() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));

        LabeledDenseMatrix<String> ldm = underTest.getSimilarities(
                startTimeStr, endTimeStr, DimensionType.USER.getDimensionName(),
                DimensionType.EMOJI.getDimensionName());
        assertEquals(4, ldm.getLabels().size());
        assertEquals(4, ldm.getMatrix().length);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetSimilarities_badCombination() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        underTest.getSimilarities(startTimeStr, endTimeStr,
                                  DimensionType.ROOM.getDimensionName(),
                                  DimensionType.ROOM.getDimensionName());
    }

    @Test
    public void testGetActive_userToTV() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Double> scores = underTest.getActive(startTimeStr, endTimeStr,
                                                         DimensionType.USER.toString(),
                                                         ActiveMethod.ToTV.toString(), "10");

        assertFalse(scores.isEmpty());
    }

    @Test
    public void testGetActive_roomToTV() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Double> scores = underTest.getActive(startTimeStr, endTimeStr,
                                                         DimensionType.ROOM.toString(),
                                                         ActiveMethod.ToTV.toString(), "10");

        assertFalse(scores.isEmpty());
    }

    @Test
    public void testGetActive_userInvalidMethod() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Double> scores = underTest.getActive(startTimeStr, endTimeStr,
                                                         DimensionType.USER.toString(),
                                                         ActiveMethod.ToMV.toString(), "10");
        assertFalse(scores.isEmpty());
    }

    @Test
    public void testGetActive_roomInvalidMethod() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Double> scores =  underTest.getActive(startTimeStr, endTimeStr,
                                                          DimensionType.ROOM.toString(),
                                                          ActiveMethod.ToMV.toString(), "10");
        assertFalse(scores.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetActive_invalidDimension() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        underTest.getActive(startTimeStr, endTimeStr, DimensionType.EMOJI.toString(),
                                ActiveMethod.ToTV.toString(), "10");
    }

    @Test
    public void testGetEmojiIcons() {
        Map<String, String> emojis = ImmutableMap.of("emoji1", "http://emoji1.com",
                                                     "emoji2", "http://emoji2.com");
        when(chatApiDao.getEmojis()).thenReturn(emojis);

        EmojiMap result = underTest.getEmojiIcons();
        assertEquals(emojis, result.getCustomEmojis());
        assertNotNull(result.getUnicodeEmojis());
        ObjectMapper objectMapper = JsonObjectMapperFactory.createObjectMapper();
        Map<String, String> expectedUnicodeEmojis = LocalEmojiUtils.getUnicodeEmojis(objectMapper);
        assertEquals(expectedUnicodeEmojis, result.getUnicodeEmojis());
    }

    @After
    public void tearDown() throws Exception {
        EntityManager em = ChatAlyticsDAOFactory.getEntityManagerFactory(config)
                                                .createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM " + EmojiEntity.EMOJI_TABLE_NAME).executeUpdate();
        em.getTransaction().commit();
        entityDao.stopAsync().awaitTerminated();
    }

    private void storeTestEmojis(List<EmojiEntity> emojis) {
        for (EmojiEntity emoji : emojis) {
            entityDao.persistEmoji(emoji);
        }
    }
}
