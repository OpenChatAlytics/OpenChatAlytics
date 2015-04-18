package com.hipchalytics.compute.db.dao;

import com.google.common.base.Optional;
import com.hipchalytics.compute.config.HipChalyticsConfig;
import com.hipchalytics.core.model.HipchatEntity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link HipChalyticsDaoImpl}
 *
 * @author giannis
 *
 */
public class HipChalyticsDaoImplTest {

    private HipChalyticsDaoImpl underTest;
    private DateTime mentionDate;

    @Before
    public void setUp() throws Exception {
        HipChalyticsConfig config = new HipChalyticsConfig();
        config.persistenceUnitName = "hipchalytics-db-test";
        underTest = new HipChalyticsDaoImpl(config);
        underTest.startAsync().awaitRunning();

        // Insert a bunch of test values
        mentionDate = new DateTime().withZone(DateTimeZone.UTC);
        underTest.persistEntity(new HipchatEntity("entity1", 1, mentionDate, "giannis", "room1"));
        underTest.persistEntity(new HipchatEntity("entity2", 1, mentionDate, "giannis", "room1"));
        underTest.persistEntity(new HipchatEntity("entity1", 1, mentionDate, "giannis", "room2"));
        underTest.persistEntity(new HipchatEntity("entity1", 1, mentionDate, "jane", "room1"));
    }

    /**
     * Makes sure that the last message pull dates are correct
     */
    @Test
    public void testGetLastMessagePullTime() {
        DateTime expectedDate = new DateTime(0).withZone(DateTimeZone.UTC);
        assertEquals(expectedDate, underTest.getLastMessagePullTime());
        expectedDate = expectedDate.plusHours(1);
        underTest.setLastMessagePullTime(expectedDate);
        assertEquals(expectedDate, underTest.getLastMessagePullTime());
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
        long result = underTest.getTotalMentionsForEntity("entity1", timeInterval,
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
        List<HipchatEntity> result = underTest.getAllMentionsForEntity("entity1", timeInterval,
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

    @After
    public void tearDown() throws Exception {
        underTest.stopAsync().awaitTerminated();
    }

}
