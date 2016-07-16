package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.ChatEntity;
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
 * Tests {@link IEntityDAO}
 *
 * @author giannis
 */
public class EntityDAOImplTest {

    private IEntityDAO underTest;
    private DateTime mentionDate;
    private ChatAlyticsConfig config;

    @Before
    public void setUp() throws Exception {
        config = new ChatAlyticsConfig();
        config.persistenceUnitName = "chatalytics-db-test";
        underTest = ChatAlyticsDAOFactory.createEntityDAO(config);
        underTest.startAsync().awaitRunning();
        IMessageSummaryDAO msgSummaryDao = ChatAlyticsDAOFactory.createMessageSummaryDAO(config);
        msgSummaryDao.startAsync().awaitRunning();
        mentionDate = DateTime.now(DateTimeZone.UTC);

        // Insert a bunch of test values
        underTest.persistEntity(new ChatEntity("giannis", "room1", mentionDate, "e1", 1, false));
        underTest.persistEntity(new ChatEntity("giannis", "room1", mentionDate, "e2", 1, false));
        underTest.persistEntity(new ChatEntity("giannis", "room2", mentionDate, "e1", 1, false));
        underTest.persistEntity(new ChatEntity("jane", "room1", mentionDate, "e1", 1, false));

        msgSummaryDao.persistMessageSummary(new MessageSummary("giannis", "room1", mentionDate,
                                                               MessageType.MESSAGE, 10, false));
        msgSummaryDao.persistMessageSummary(new MessageSummary("jane", "room1", mentionDate,
                                                               MessageType.MESSAGE, 10, false));
        msgSummaryDao.stopAsync().awaitTerminated();
    }

    @Test
    public void testPersistEntity_withDuplicate() {
        DateTime mentionTime = DateTime.now();
        ChatEntity entity = new ChatEntity("user", "testroom", mentionTime, "test_value", 1, false);
        underTest.persistEntity(entity);
        ChatEntity existingEntity = underTest.getEntity(entity);
        assertNotNull(existingEntity);
        assertEquals(1, existingEntity.getOccurrences());

        // insert it again
        entity = new ChatEntity("user", "testroom", mentionTime, "test_value", 1, false);
        underTest.persistEntity(entity);
        existingEntity = underTest.getEntity(entity);
        assertEquals(2, existingEntity.getOccurrences());
    }

    /**
     * Makes sure that the correct entity occurrence sums are returned.
     */
    @Test
    public void testGetTotalMentionsForEntity() {

        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));

        // Make sure that 0 is returned when nothing is found
        assertEquals(0, underTest.getTotalMentionsForEntity("unknownentity",
                                                            timeInterval,
                                                            ImmutableList.of(),
                                                            ImmutableList.of()));

        // make sure that the sums are correct for a bunch of different queries
        int result = underTest.getTotalMentionsForEntity("e1", timeInterval,
                                                          ImmutableList.of(), ImmutableList.of());
        assertEquals(3, result);

        result = underTest.getTotalMentionsForEntity("e1", timeInterval,
                                                     ImmutableList.of("room1"), ImmutableList.of());
        assertEquals(2, result);

        result = underTest.getTotalMentionsForEntity("e1", timeInterval,
                                                     ImmutableList.of("room1"),
                                                     ImmutableList.of("giannis"));
        assertEquals(1, result);
    }

    /**
     * Makes sure that the correct amount of entity mentions are returned
     */
    @Test
    public void testGetAllMentionsForEntity() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        List<ChatEntity> result = underTest.getAllMentionsForEntity("e1", timeInterval,
                                                                    ImmutableList.of(),
                                                                    ImmutableList.of());
        assertEquals(3, result.size());

        result = underTest.getAllMentionsForEntity("e1", timeInterval,
                                                   ImmutableList.of("room1"), ImmutableList.of());
        assertEquals(2, result.size());

        result = underTest.getAllMentionsForEntity("e1", timeInterval,
                                                   ImmutableList.of("room1"),
                                                   ImmutableList.of("giannis"));
        assertEquals(1, result.size());
    }

    /**
     * Makes sure that the correct amount of all entity mentions are returned
     */
    @Test
    public void testGetAllMentions() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        List<ChatEntity> result = underTest.getAllMentions(timeInterval, ImmutableList.of(),
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
    public void testGetTopEntities() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        Map<String, Long> result =
            underTest.getTopEntities(timeInterval, ImmutableList.of(), ImmutableList.of(), 10);
        assertEquals(2, result.size());
        assertEquals(3L, result.get("e1").longValue());
        assertEquals(1L, result.get("e2").longValue());

        result = underTest.getTopEntities(timeInterval, ImmutableList.of("room1"),
                                          ImmutableList.of(), 10);
        assertEquals(2, result.size());
        assertEquals(2L, result.get("e1").longValue());
        assertEquals(1L, result.get("e2").longValue());

        result = underTest.getTopEntities(timeInterval, ImmutableList.of("room1"),
                                          ImmutableList.of("giannis"), 10);
        assertEquals(2, result.size());
        assertEquals(1L, result.get("e1").longValue());
        assertEquals(1L, result.get("e2").longValue());
    }

    @Test
    public void testGetRoomSimilaritiesByEntity() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        LabeledDenseMatrix<String> result = underTest.getRoomSimilaritiesByEntity(timeInterval);
        assertEquals(2, result.getLabels().size());
        assertEquals(2, result.getMatrix().length);
    }

    @Test
    public void testGetUserSimilaritiesByEntity() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        LabeledDenseMatrix<String> result = underTest.getUserSimilaritiesByEntity(timeInterval);
        assertEquals(2, result.getLabels().size());
        assertEquals(2, result.getMatrix().length);
    }

    @Test
    public void testGetTopRoomsByMethod() {
        Interval interval = new Interval(mentionDate.minusMillis(1), mentionDate.plusMillis(1));

        Map<String, Double> result =
                underTest.getActiveRoomsByMethod(interval, ActiveMethod.ToTV, 10);
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
        em.createNativeQuery("DELETE FROM " + ChatEntity.ENTITY_TABLE_NAME).executeUpdate();
        em.createNativeQuery("DELETE FROM " + MessageSummary.MESSAGE_SUMMARY_TABLE_NAME)
          .executeUpdate();
        em.getTransaction().commit();
        underTest.stopAsync().awaitTerminated();
    }
}
