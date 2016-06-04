package com.chatalytics.web.resources;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IEntityDAO;
import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.DimensionType;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.ChatEntity;
import com.chatalytics.web.constant.ActiveMethod;
import com.chatalytics.web.constant.WebConstants;
import com.chatalytics.web.utils.DateTimeUtils;
import com.chatalytics.web.utils.ResourceUtils;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Optional;

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
    public static final String FIRST_SIMILARITY_DIM_PARAM = "firstDim";
    public static final String SECOND_SIMILARITY_DIM_PARAM = "secondDim";
    public static final String DIM_PARAM = "dimension";
    public static final String METHOD = "method";

    private static final int MAX_RESULTS = 20;
    private static final Logger LOG = LoggerFactory.getLogger(EntitiesResource.class);

    private final IEntityDAO entityDao;
    private final DateTimeZone dtz;

    public EntitiesResource(ChatAlyticsConfig config) {
        entityDao = ChatAlyticsDAOFactory.createEntityDAO(config);
        dtz = DateTimeZone.forID(config.timeZone);
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
        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);

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
        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);

        return entityDao.getAllMentions(interval, roomName, username);
    }

    @GET
    @Path("similarities")
    @Produces(MediaType.APPLICATION_JSON)
    public LabeledDenseMatrix<String> getSimilarities(
            @QueryParam(START_TIME_PARAM) String startTimeStr,
            @QueryParam(END_TIME_PARAM) String endTimeStr,
            @QueryParam(FIRST_SIMILARITY_DIM_PARAM) String firstDimStr,
            @QueryParam(SECOND_SIMILARITY_DIM_PARAM) String secondDimStr) {

        LOG.debug("Got a call for dimensions {} and {} with starttime={}, endtime={}",
                  firstDimStr, secondDimStr, startTimeStr, endTimeStr);

        DimensionType firstDim = DimensionType.fromDimensionName(firstDimStr);
        DimensionType secondDim = DimensionType.fromDimensionName(secondDimStr);
        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);

        if (firstDim == DimensionType.ROOM && secondDim == DimensionType.ENTITY) {
            return entityDao.getRoomSimilaritiesByEntity(interval);
        } else {
            String formatStr = "The dimension combination you specified (%s, %s) is not supported";
            throw new UnsupportedOperationException(String.format(formatStr, firstDimStr,
                                                                  secondDimStr));
        }
    }

    @GET
    @Path("active")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Double> getMostActive(@QueryParam(START_TIME_PARAM) String startTimeStr,
                                             @QueryParam(END_TIME_PARAM) String endTimeStr,
                                             @QueryParam(DIM_PARAM) String dimensionStr,
                                             @QueryParam(METHOD) String methodStr,
                                             @QueryParam(TOP_N) String topNStr) {

        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);
        DimensionType dimension = DimensionType.fromDimensionName(dimensionStr);
        ActiveMethod method = ActiveMethod.fromMethodName(methodStr);
        Optional<Integer> topN = ResourceUtils.getOptionalForParameterAsInt(topNStr);

        if (dimension == DimensionType.ROOM) {
            if (method == ActiveMethod.ToTV) {
                return entityDao.getTopRoomsByEoTV(interval, topN.or(MAX_RESULTS));
            } else {
                throw new UnsupportedOperationException("Unrecognized method: " + method);
            }
        } else if (dimension == DimensionType.USER) {
            if (method == ActiveMethod.ToTV) {
                return entityDao.getTopUsersByEoTV(interval, topN.or(MAX_RESULTS));
            } else {
                throw new UnsupportedOperationException("Unrecognized method: " + method);
            }

        } else {
            String formatMsg = "The dimension %s you provided is not supported. Pass in %s or %s";
            throw new UnsupportedOperationException(String.format(formatMsg, dimensionStr,
                                                                  DimensionType.ROOM,
                                                                  DimensionType.USER));
        }
    }
}
