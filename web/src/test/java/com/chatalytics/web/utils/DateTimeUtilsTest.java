package com.chatalytics.web.utils;

import com.chatalytics.core.config.ChatAlyticsConfig;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link DateTimeUtils}.
 *
 * @author giannis
 *
 */
public class DateTimeUtilsTest {

    /**
     * Tests to see if the correct date with the right timezone is returned when parsing parameters
     */
    @Test
    public void testGetDateTimeFromParameter() {
        DateTimeZone dtZone = DateTimeZone.forID(new ChatAlyticsConfig().timeZone);

        DateTime dateTime = DateTimeUtils.getDateTimeFromParameter("2015-01-01", dtZone);
        DateTime expectedDateTime =
            new DateTime(2015, 1, 1, 0, 0).withZone(dtZone).toDateTime(DateTimeZone.UTC);
        assertEquals(expectedDateTime, dateTime);
    }
}
