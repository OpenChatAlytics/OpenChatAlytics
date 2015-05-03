package com.hipchalytics.web.resources;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.hipchalytics.compute.config.HipChalyticsConfig;
import com.hipchalytics.compute.db.dao.HipChalyticsDaoFactory;
import com.hipchalytics.compute.db.dao.IHipChalyticsDao;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST endpoint for getting trending topics collected from chat messages
 *
 * @author giannis
 *
 */
@Path(TrendingTopicsResource.TRENDING_ENDPOINT)
public class TrendingTopicsResource {

    public static final String TRENDING_ENDPOINT = "trending";
    public static final String START_TIME_PARAM = "starttime";
    public static final String END_TIME_PARAM = "endtime";
    public static final String USER_PARAM = "user";
    public static final String ROOM_PARAM = "room";

    private static final String PARAMETER_WITH_DAY_DTF_STR = "YYYY-MM-dd";
    public static final DateTimeFormatter PARAMETER_WITH_DAY_DTF =
            DateTimeFormat.forPattern(PARAMETER_WITH_DAY_DTF_STR).withZoneUTC();
    private static final String PARAMETER_WITH_HOUR_DTF_STR = "YYYY-MM-dd_HH";
    public static final DateTimeFormatter PARAMETER_WITH_HOUR_DTF =
            DateTimeFormat.forPattern(PARAMETER_WITH_HOUR_DTF_STR).withZoneUTC();

    private static final int MAX_RESULTS = 10;
    private static final Logger LOG = LoggerFactory.getLogger(TrendingTopicsResource.class);

    private final IHipChalyticsDao dbDao;
    private final DateTimeZone dateTimeZone;
    private final ObjectMapper objectMapper;

    public TrendingTopicsResource(HipChalyticsConfig hconfig) {
        dbDao = HipChalyticsDaoFactory.getHipchalyticsDao(hconfig);
        dateTimeZone = DateTimeZone.forID(hconfig.timeZone);
        objectMapper = new ObjectMapper();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTrendingTopics(@FormParam(START_TIME_PARAM) String startTimeStr,
                                      @FormParam(END_TIME_PARAM) String endTimeStr,
                                      @FormParam(USER_PARAM) String user,
                                      @FormParam(ROOM_PARAM) String room)
            throws JsonGenerationException, JsonMappingException, IOException {
        LOG.debug("Got query for starttime={}, endtime={}, user={}, room={}",
                  startTimeStr, endTimeStr, user, room);

        Optional<String> username = getOptionalForParameter(user);
        Optional<String> roomName = getOptionalForParameter(room);

        DateTime startTime = getDateTimeFromParameter(startTimeStr);
        DateTime endTime = getDateTimeFromParameter(endTimeStr);
        Interval interval = new Interval(startTime, endTime);

        Map<String, Long> topEntities = dbDao.getTopEntities(interval, roomName,
                                                             username, MAX_RESULTS);
        String jsonResult = objectMapper.writeValueAsString(topEntities);
        return Response.ok(jsonResult).build();
    }

    /**
     * Helper method that returns an {@link Optional} with the value set if the parameter is not
     * null or non-empty.
     *
     * @param parameterStr
     *            The parameter to create the {@link Optional} for.
     * @return An {@link Optional} with the value set or absent appropriately.
     */
    private Optional<String> getOptionalForParameter(String parameterStr) {
        if (parameterStr == null || parameterStr.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.of(parameterStr);
        }
    }

    /**
     * Helper method that parses a date time string and returns an actual {@link DateTime} object.
     *
     * @param dateTimeStr
     *            The string parameter to parse. It has to be in one of the following supported
     *            formats:
     *            <ol>
     *            <li>{@value #PARAMETER_WITH_DAY_DTF_STR}</li>
     *            <li>{@value #PARAMETER_WITH_HOUR_DTF_STR}</li>
     *            </ol>
     * @return A {@link DateTime} object
     */
    @VisibleForTesting
    protected DateTime getDateTimeFromParameter(String dateTimeStr) {
        Preconditions.checkNotNull(dateTimeStr,
                                   "Both start and end time date parameters cannot be null");
        Preconditions.checkArgument(dateTimeStr.length() == PARAMETER_WITH_DAY_DTF_STR.length() ||
                                    dateTimeStr.length() == PARAMETER_WITH_HOUR_DTF_STR.length(),
                                    String.format("Time parameters have to be of the form {} or {}",
                                                  PARAMETER_WITH_DAY_DTF_STR,
                                                  PARAMETER_WITH_HOUR_DTF_STR));

        // fix time zone based on config and parse date
        DateTimeFormatter zoneAdjustedDtf;
        if (dateTimeStr.length() == PARAMETER_WITH_DAY_DTF_STR.length()) {
            zoneAdjustedDtf = PARAMETER_WITH_DAY_DTF.withZone(dateTimeZone);
        } else {
            zoneAdjustedDtf = PARAMETER_WITH_HOUR_DTF.withZone(dateTimeZone);
        }

        // convert to UTC since all the dates in the DB are stored with UTC
        return zoneAdjustedDtf.parseDateTime(dateTimeStr).toDateTime(DateTimeZone.UTC);
    }
}
