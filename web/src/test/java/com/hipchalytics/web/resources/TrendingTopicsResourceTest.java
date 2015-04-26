package com.hipchalytics.web.resources;

import com.google.common.collect.Lists;
import com.hipchalytics.compute.config.HipChalyticsConfig;
import com.hipchalytics.compute.db.dao.HipChalyticsDaoImpl;
import com.hipchalytics.compute.db.dao.IHipChalyticsDao;
import com.hipchalytics.core.model.HipchatEntity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link TrendingTopicsResource}.
 *
 * @author giannis
 *
 */
public class TrendingTopicsResourceTest {

    private IHipChalyticsDao hipchalyticsDao;
    private TrendingTopicsResource underTest;
    private DateTimeZone dtZone;
    private DateTime mentionTime;

    @Before
    public void setUp() throws Exception {
        HipChalyticsConfig config = new HipChalyticsConfig();
        config.persistenceUnitName = "hipchalytics-web-test";
        config.timeZone = "America/New_York";
        dtZone = DateTimeZone.forID(config.timeZone);

        hipchalyticsDao = new HipChalyticsDaoImpl(config);
        hipchalyticsDao.startAsync().awaitRunning();

        mentionTime = DateTime.now().withZone(DateTimeZone.UTC);
        List<HipchatEntity> entities = Lists.newArrayListWithCapacity(10);
        entities.add(new HipchatEntity("e1", 5, mentionTime.minusHours(1), "u1", "r1"));
        entities.add(new HipchatEntity("e1", 4, mentionTime.minusHours(1), "u2", "r1"));
        entities.add(new HipchatEntity("e1", 1, mentionTime.minusHours(2), "u3", "r2"));
        entities.add(new HipchatEntity("e1", 1, mentionTime.minusHours(3), "u4", "r3"));
        entities.add(new HipchatEntity("e2", 1, mentionTime.minusHours(1), "u1", "r1"));
        entities.add(new HipchatEntity("e2", 6, mentionTime.minusHours(1), "u2", "r1"));
        entities.add(new HipchatEntity("e2", 7, mentionTime.minusHours(2), "u3", "r2"));
        entities.add(new HipchatEntity("e3", 3, mentionTime.minusHours(1), "u2", "r2"));
        entities.add(new HipchatEntity("e3", 3, mentionTime.minusHours(2), "u3", "r1"));
        entities.add(new HipchatEntity("e4", 3, mentionTime.minusHours(1), "u1", "r4"));
        storeTestEntities(entities);
        underTest = new TrendingTopicsResource(config);
    }

    private void storeTestEntities(List<HipchatEntity> entities) {
        for (HipchatEntity entity : entities) {
            hipchalyticsDao.persistEntity(entity);
        }
    }

    @After
    public void tearDown() throws Exception {
        hipchalyticsDao.stopAsync().awaitTerminated();
    }

    /**
     * Tests to see if the correct trending topics are returned
     */
    @Test
    public void testGetTrendingTopics() throws Exception {
        DateTimeFormatter dtf = TrendingTopicsResource.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Response response = underTest.getTrendingTopics(startTimeStr, endTimeStr, "u1", "r1");
        assertEquals("{\"e1\":5,\"e2\":1}", response.getEntity());

        response = underTest.getTrendingTopics(startTimeStr, endTimeStr, "u1", null);
        assertEquals("{\"e1\":5,\"e4\":3,\"e2\":1}", response.getEntity());

        response = underTest.getTrendingTopics(startTimeStr, endTimeStr, null, null);
        assertEquals("{\"e2\":14,\"e1\":11,\"e3\":6,\"e4\":3}", response.getEntity());
    }

    /**
     * Tests to see if the correct date with the right timezone is returned when parsing parameters
     */
    @Test
    public void testGetDateTimeFromParameter() {
        DateTime dateTime = underTest.getDateTimeFromParameter("2015-01-01");
        DateTime expectedDateTime =
            new DateTime(2015, 1, 1, 0, 0).withZone(dtZone).toDateTime(DateTimeZone.UTC);
        assertEquals(expectedDateTime, dateTime);
    }

}
