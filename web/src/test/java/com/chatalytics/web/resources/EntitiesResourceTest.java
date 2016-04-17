package com.chatalytics.web.resources;

import com.chatalytics.compute.db.dao.EntityDAOImpl;
import com.chatalytics.compute.db.dao.IEntityDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.ChatEntity;
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
 * Tests {@link EntitiesResource}.
 *
 * @author giannis
 *
 */
public class EntitiesResourceTest {

    private IEntityDAO entityDao;
    private EntitiesResource undertest;
    private DateTimeZone dtZone;
    private DateTime mentionTime;

    @Before
    public void setUp() throws Exception {
        ChatAlyticsConfig config = new ChatAlyticsConfig();
        config.persistenceUnitName = "chatalytics-web-test";
        config.timeZone = "America/New_York";
        dtZone = DateTimeZone.forID(config.timeZone);

        entityDao = new EntityDAOImpl(config);
        entityDao.startAsync().awaitRunning();

        mentionTime = DateTime.now().withZone(DateTimeZone.UTC);
        List<ChatEntity> entities = Lists.newArrayListWithCapacity(10);
        entities.add(new ChatEntity("e1", 5, mentionTime.minusHours(1), "u1", "r1"));
        entities.add(new ChatEntity("e1", 4, mentionTime.minusHours(1), "u2", "r1"));
        entities.add(new ChatEntity("e1", 1, mentionTime.minusHours(2), "u3", "r2"));
        entities.add(new ChatEntity("e1", 1, mentionTime.minusHours(3), "u4", "r3"));
        entities.add(new ChatEntity("e2", 1, mentionTime.minusHours(1), "u1", "r1"));
        entities.add(new ChatEntity("e2", 6, mentionTime.minusHours(1), "u2", "r1"));
        entities.add(new ChatEntity("e2", 7, mentionTime.minusHours(2), "u3", "r2"));
        entities.add(new ChatEntity("e3", 3, mentionTime.minusHours(1), "u2", "r2"));
        entities.add(new ChatEntity("e3", 3, mentionTime.minusHours(2), "u3", "r1"));
        entities.add(new ChatEntity("e4", 3, mentionTime.minusHours(1), "u1", "r4"));
        storeTestEntities(entities);
        undertest = new EntitiesResource(config);
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
        Map<String, Long> response = undertest.getTrendingTopics(startTimeStr, endTimeStr, "u1",
                                                                 "r1", null);
        Map<String, Long> expected = Maps.newHashMap();
        expected.put("e1", 5L);
        expected.put("e2", 1L);
        assertEquals(expected, response);

        response = undertest.getTrendingTopics(startTimeStr, endTimeStr, "u1", null, null);
        expected.clear();
        expected.put("e1", 5L);
        expected.put("e4", 3L);
        expected.put("e2", 1L);
        assertEquals(expected, response);

        response = undertest.getTrendingTopics(startTimeStr, endTimeStr, null, null, null);
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
