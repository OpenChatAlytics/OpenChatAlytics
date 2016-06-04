package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.ChatEntity;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.model.data.MessageType;
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
        underTest.persistEntity(new ChatEntity("entity1", 1, mentionDate, "giannis", "room1"));
        underTest.persistEntity(new ChatEntity("entity2", 1, mentionDate, "giannis", "room1"));
        underTest.persistEntity(new ChatEntity("entity1", 1, mentionDate, "giannis", "room2"));
        underTest.persistEntity(new ChatEntity("entity1", 1, mentionDate, "jane", "room1"));

        msgSummaryDao.persistMessageSummary(new MessageSummary("giannis", "room1", mentionDate,
                                                               MessageType.MESSAGE, 10));
        msgSummaryDao.persistMessageSummary(new MessageSummary("jane", "room1", mentionDate,
                                                               MessageType.MESSAGE, 10));
        msgSummaryDao.stopAsync().awaitTerminated();
    }

    @Test
    public void testPersistEntity_withDuplicate() {
        ChatEntity entity = new ChatEntity("test_value", 1, DateTime.now(), "user", "testroom");
        underTest.persistEntity(entity);
        ChatEntity existingEntity = underTest.getEntity(entity);
        assertNotNull(existingEntity);
        assertEquals(1, existingEntity.getOccurrences());

        // insert it again
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
                                                            Optional.absent(),
                                                            Optional.absent()));

        // make sure that the sums are correct for a bunch of different queries
        int result = underTest.getTotalMentionsForEntity("entity1", timeInterval,
                                                          Optional.absent(), Optional.absent());
        assertEquals(3, result);

        result = underTest.getTotalMentionsForEntity("entity1", timeInterval,
                                                     Optional.of("room1"), Optional.absent());
        assertEquals(2, result);

        result = underTest.getTotalMentionsForEntity("entity1", timeInterval,
                                                     Optional.of("room1"), Optional.of("giannis"));
        assertEquals(1, result);
    }

    /**
     * Makes sure that the correct amount of entity mentions are returned
     */
    @Test
    public void testGetAllMentionsForEntity() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        List<ChatEntity> result = underTest.getAllMentionsForEntity("entity1", timeInterval,
                                                                    Optional.absent(),
                                                                    Optional.absent());
        assertEquals(3, result.size());

        result = underTest.getAllMentionsForEntity("entity1", timeInterval, Optional.of("room1"),
                                                Optional.absent());
        assertEquals(2, result.size());

        result = underTest.getAllMentionsForEntity("entity1", timeInterval, Optional.of("room1"),
                                                Optional.of("giannis"));
        assertEquals(1, result.size());
    }

    /**
     * Makes sure that the correct amount of all entity mentions are returned
     */
    @Test
    public void testGetAllMentions() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        List<ChatEntity> result = underTest.getAllMentions(timeInterval, Optional.absent(),
                                                           Optional.absent());
        assertEquals(4, result.size());

        result = underTest.getAllMentions(timeInterval, Optional.of("room1"), Optional.absent());
        assertEquals(3, result.size());

        result = underTest.getAllMentions(timeInterval, Optional.of("room1"),
                                          Optional.of("giannis"));
        assertEquals(2, result.size());
    }

    @Test
    public void testGetTopEntities() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        Map<String, Long> result =
            underTest.getTopEntities(timeInterval, Optional.absent(), Optional.absent(), 10);
        assertEquals(2, result.size());
        assertEquals(3L, result.get("entity1").longValue());
        assertEquals(1L, result.get("entity2").longValue());

        result = underTest.getTopEntities(timeInterval, Optional.of("room1"), Optional.absent(), 10);
        assertEquals(2, result.size());
        assertEquals(2L, result.get("entity1").longValue());
        assertEquals(1L, result.get("entity2").longValue());

        result = underTest.getTopEntities(timeInterval, Optional.of("room1"),
                                          Optional.of("giannis"), 10);
        assertEquals(2, result.size());
        assertEquals(1L, result.get("entity1").longValue());
        assertEquals(1L, result.get("entity2").longValue());
    }

    @Test
    public void testGetRoomSimilaritiesByEntity() {
        Interval timeInterval = new Interval(mentionDate, mentionDate.plusHours(3));
        LabeledDenseMatrix<String> result = underTest.getRoomSimilaritiesByEntity(timeInterval);
        assertEquals(2, result.getLabels().size());
        assertEquals(2, result.getMatrix().length);
    }

    @Test
    public void testGetTopRoomsByMethod() {
        Interval interval = new Interval(mentionDate.minusMillis(1), mentionDate.plusMillis(1));

        Map<String, Double> result = underTest.getTopRoomsByMethod(interval, ActiveMethod.ToTV, 10);
        assertEquals(2, result.size());
        assertEquals(0.75, result.get("room1"), 0);
        assertEquals(0.25, result.get("room2"), 0);

        result = underTest.getTopRoomsByMethod(interval, ActiveMethod.ToMV, 10);
        assertEquals(2, result.size());
        assertEquals(0.15, result.get("room1"), 0);
        assertEquals(0.05, result.get("room2"), 0);
    }

    @Test
    public void testGetTopUsersByMethod() {
        Interval interval = new Interval(mentionDate.minusMillis(1), mentionDate.plusMillis(1));

        Map<String, Double> result = underTest.getTopUsersByMethod(interval, ActiveMethod.ToTV, 10);
        assertEquals(2, result.size());
        assertEquals(0.75, result.get("giannis"), 0);
        assertEquals(0.25, result.get("jane"), 0);

        result = underTest.getTopUsersByMethod(interval, ActiveMethod.ToMV, 10);
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
