package com.chatalytics.web.resources;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IEntityDAO;
import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.DimensionType;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.ChatEntity;
import com.chatalytics.web.utils.DateTimeUtils;
import com.google.common.collect.ImmutableList;
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
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link EntitiesResource}.
 *
 * @author giannis
 *
 */
public class EntitiesResourceTest {

    private IEntityDAO entityDao;
    private EntitiesResource underTest;
    private DateTimeZone dtZone;
    private DateTime mentionTime;
    private List<ChatEntity> entities;
    private ChatAlyticsConfig config;

    @Before
    public void setUp() throws Exception {
        config = new ChatAlyticsConfig();
        config.persistenceUnitName = "chatalytics-web-test";
        config.timeZone = "America/New_York";
        dtZone = DateTimeZone.forID(config.timeZone);

        entityDao = ChatAlyticsDAOFactory.createEntityDAO(config);
        entityDao.startAsync().awaitRunning();

        mentionTime = DateTime.now().withZone(DateTimeZone.UTC);
        this.entities = Lists.newArrayListWithCapacity(10);
        entities.add(new ChatEntity("u1", "r1", mentionTime.minusHours(1), "e1", 5));
        entities.add(new ChatEntity("u2", "r1", mentionTime.minusHours(1), "e1", 4));
        entities.add(new ChatEntity("u3", "r2", mentionTime.minusHours(1), "e1", 1));
        entities.add(new ChatEntity("u4", "r3", mentionTime.minusHours(3), "e1", 1));
        entities.add(new ChatEntity("u1", "r1", mentionTime.minusHours(1), "e2", 1));
        entities.add(new ChatEntity("u2", "r1", mentionTime.minusHours(1), "e2", 6));
        entities.add(new ChatEntity("u3", "r2", mentionTime.minusHours(2), "e2", 7));
        entities.add(new ChatEntity("u2", "r2", mentionTime.minusHours(1), "e3", 3));
        entities.add(new ChatEntity("u3", "r1", mentionTime.minusHours(2), "e3", 3));
        entities.add(new ChatEntity("u1", "r4", mentionTime.minusHours(1), "e4", 3));
        storeTestEntities(entities);
        underTest = new EntitiesResource(config);
    }

    private void storeTestEntities(List<ChatEntity> entities) {
        for (ChatEntity entity : entities) {
            entityDao.persistEntity(entity);
        }
    }

    /**
     * Tests to see if the correct trending topics are returned
     */
    @Test
    public void testGetTrendingTopics() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Long> response = underTest.getTrendingTopics(startTimeStr, endTimeStr,
                                                                 ImmutableList.of("u1"),
                                                                 ImmutableList.of("r1"), null);
        Map<String, Long> expected = Maps.newHashMap();
        expected.put("e1", 5L);
        expected.put("e2", 1L);
        assertEquals(expected, response);

        response = underTest.getTrendingTopics(startTimeStr, endTimeStr, ImmutableList.of("u1"),
                                               null, null);
        expected.clear();
        expected.put("e1", 5L);
        expected.put("e4", 3L);
        expected.put("e2", 1L);
        assertEquals(expected, response);

        response = underTest.getTrendingTopics(startTimeStr, endTimeStr, null, null, null);
        expected.clear();
        expected.put("e2", 14L);
        expected.put("e1", 11L);
        expected.put("e3", 6L);
        expected.put("e4", 3L);
        assertEquals(expected, response);
    }

    @Test
    public void testGetAllEntities() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        List<ChatEntity> result = underTest.getAllEntites(startTimeStr, endTimeStr, null, null);
        assertEquals(entities.size(), result.size());

        Set<ChatEntity> resultEntitySet = Sets.newHashSet(result);
        for (ChatEntity expectedEntity : entities) {
            assertTrue(resultEntitySet.contains(expectedEntity));
        }

    }

    /**
     * Tests the similarities endpoint
     */
    @Test
    public void testGetSimilarities_RoomByEntity() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));

        LabeledDenseMatrix<String> ldm = underTest.getSimilarities(
                startTimeStr, endTimeStr, DimensionType.ROOM.getDimensionName(),
                DimensionType.ENTITY.getDimensionName());
        assertEquals(4, ldm.getLabels().size());
        assertEquals(4, ldm.getMatrix().length);
    }

    /**
     * Tests the similarities endpoint
     */
    @Test
    public void testGetSimilarities_UserByEntity() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));

        LabeledDenseMatrix<String> ldm = underTest.getSimilarities(
                startTimeStr, endTimeStr, DimensionType.USER.getDimensionName(),
                DimensionType.ENTITY.getDimensionName());
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
    public void testGetActive_userTOmV() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Double> scores = underTest.getActive(startTimeStr, endTimeStr,
                                                         DimensionType.USER.toString(),
                                                         ActiveMethod.ToMV.toString(), "10");
        assertFalse(scores.isEmpty());
    }

    @Test
    public void testGetActive_roomToMV() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Double> scores = underTest.getActive(startTimeStr, endTimeStr,
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

    @After
    public void tearDown() throws Exception {
        EntityManager em = ChatAlyticsDAOFactory.getEntityManagerFactory(config)
                                                .createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM " + ChatEntity.ENTITY_TABLE_NAME).executeUpdate();
        em.getTransaction().commit();
        entityDao.stopAsync().awaitTerminated();
    }

}
