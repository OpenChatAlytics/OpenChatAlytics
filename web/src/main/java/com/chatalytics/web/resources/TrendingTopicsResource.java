package com.chatalytics.web.resources;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IEntityDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.json.JsonObjectMapperFactory;
import com.chatalytics.web.utils.DateTimeUtils;
import com.chatalytics.web.utils.ResourceUtils;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
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

    private static final int MAX_RESULTS = 10;
    private static final Logger LOG = LoggerFactory.getLogger(TrendingTopicsResource.class);

    private final IEntityDAO entityDao;
    private final DateTimeZone dtZone;
    private final ObjectMapper objectMapper;

    public TrendingTopicsResource(ChatAlyticsConfig config) {
        entityDao = ChatAlyticsDAOFactory.getEntityDAO(config);
        dtZone = DateTimeZone.forID(config.timeZone);
        objectMapper = JsonObjectMapperFactory.createObjectMapper(config.inputType);
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

        Optional<String> username = ResourceUtils.getOptionalForParameter(user);
        Optional<String> roomName = ResourceUtils.getOptionalForParameter(room);

        DateTime startTime = DateTimeUtils.getDateTimeFromParameter(startTimeStr, dtZone);
        DateTime endTime = DateTimeUtils.getDateTimeFromParameter(endTimeStr, dtZone);
        Interval interval = new Interval(startTime, endTime);

        Map<String, Long> topEntities = entityDao.getTopEntities(interval, roomName, username,
                                                                 MAX_RESULTS);
        String jsonResult = objectMapper.writeValueAsString(topEntities);
        return Response.ok(jsonResult).build();
    }
}
