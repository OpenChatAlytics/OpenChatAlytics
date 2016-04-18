package com.chatalytics.web.resources;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IEntityDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.ChatEntity;
import com.chatalytics.web.constant.WebConstants;
import com.chatalytics.web.utils.DateTimeUtils;
import com.chatalytics.web.utils.ResourceUtils;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Optional;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * REST endpoint for getting entity stats collected from chat messages
 *
 * @author giannis
 *
 */
@Path(EntitiesResource.ENTITIES_ENDPOINT)
public class EntitiesResource {

    public static final String ENTITIES_ENDPOINT = WebConstants.API_PATH + "entities";
    public static final String START_TIME_PARAM = "starttime";
    public static final String END_TIME_PARAM = "endtime";
    public static final String USER_PARAM = "user";
    public static final String ROOM_PARAM = "room";
    public static final String TOP_N = "n";

    private static final int MAX_RESULTS = 10;
    private static final Logger LOG = LoggerFactory.getLogger(EntitiesResource.class);

    private final IEntityDAO entityDao;
    private final DateTimeZone dtZone;

    public EntitiesResource(ChatAlyticsConfig config) {
        entityDao = ChatAlyticsDAOFactory.getEntityDAO(config);
        dtZone = DateTimeZone.forID(config.timeZone);
    }

    @GET
    @Path("trending")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> getTrendingTopics(@QueryParam(START_TIME_PARAM) String startTimeStr,
                                      @QueryParam(END_TIME_PARAM) String endTimeStr,
                                      @QueryParam(USER_PARAM) String user,
                                      @QueryParam(ROOM_PARAM) String room,
                                      @QueryParam(TOP_N) String topNStr)
                    throws JsonGenerationException, JsonMappingException, IOException {

        LOG.debug("Got trending topics query for starttime={}, endtime={}, user={}, room={}",
                  startTimeStr, endTimeStr, user, room);

        Optional<String> username = ResourceUtils.getOptionalForParameter(user);
        Optional<String> roomName = ResourceUtils.getOptionalForParameter(room);
        Optional<Integer> topN = ResourceUtils.getOptionalForParameterAsInt(topNStr);

        DateTime startTime = DateTimeUtils.getDateTimeFromParameter(startTimeStr, dtZone);
        DateTime endTime = DateTimeUtils.getDateTimeFromParameter(endTimeStr, dtZone);
        Interval interval = new Interval(startTime, endTime);

        return entityDao.getTopEntities(interval, roomName, username, topN.or(MAX_RESULTS));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ChatEntity> getAllEntites(@QueryParam(START_TIME_PARAM) String startTimeStr,
                                          @QueryParam(END_TIME_PARAM) String endTimeStr,
                                          @QueryParam(USER_PARAM) String user,
                                          @QueryParam(ROOM_PARAM) String room) {

        LOG.debug("Got all entities query for starttime={}, endtime={}, user={}, room={}",
                  startTimeStr, endTimeStr, user, room);

        Optional<String> username = ResourceUtils.getOptionalForParameter(user);
        Optional<String> roomName = ResourceUtils.getOptionalForParameter(room);

        DateTime startTime = DateTimeUtils.getDateTimeFromParameter(startTimeStr, dtZone);
        DateTime endTime = DateTimeUtils.getDateTimeFromParameter(endTimeStr, dtZone);
        Interval interval = new Interval(startTime, endTime);

        return entityDao.getAllMentions(interval, roomName, username);
    }
}
