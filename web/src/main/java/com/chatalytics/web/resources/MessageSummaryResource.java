package com.chatalytics.web.resources;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IMessageSummaryDAO;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.DimensionType;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.model.data.MessageType;
import com.chatalytics.web.constant.WebConstants;
import com.chatalytics.web.utils.DateTimeUtils;
import com.chatalytics.web.utils.ResourceUtils;
import com.google.common.base.Optional;

import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import static com.chatalytics.web.constant.WebConstants.END_TIME;
import static com.chatalytics.web.constant.WebConstants.ROOM;
import static com.chatalytics.web.constant.WebConstants.START_TIME;
import static com.chatalytics.web.constant.WebConstants.TOP_N;
import static com.chatalytics.web.constant.WebConstants.USER;

/**
 * REST endpoint for getting message summary data. Check {@link MessageSummary} for all the
 * properties of a message summary.
 *
 * @author giannis
 */
@Path(MessageSummaryResource.MESSAGE_SUMMARY_ENDPOINT)
public class MessageSummaryResource {

    public static final String MESSAGE_SUMMARY_ENDPOINT = WebConstants.API_PATH + "message-summary";
    public static final String MESSAGE_TYPE = "type";
    public static final String DIM = "dimension";
    public static final String METHOD = "method";

    private static final int MAX_RESULTS = 20;
    private static final Logger LOG = LoggerFactory.getLogger(MessageSummaryResource.class);

    private final IMessageSummaryDAO msgSummaryDao;
    private final DateTimeZone dtz;

    public MessageSummaryResource(ChatAlyticsConfig config) {
        msgSummaryDao = ChatAlyticsDAOFactory.createMessageSummaryDAO(config);
        dtz = DateTimeZone.forID(config.timeZone);
     }

    /**
     * Gets the all the message summaries in a particular date range and/or user and/or room and/or
     * type. Note that the only required parameter is the time range
     *
     * @param startTimeStr
     *            The start time to get the summaries for
     * @param endTimeStr
     *            The end time to get the summaries for
     * @param users
     *            The user to query for (optional)
     * @param rooms
     *            The room to query for (optional)
     * @param msgTypeStr
     *            The type of the message. See {@link MessageType} for more info (optional)
     * @return Returns all the {@link MessageSummary}s based on the given parameters.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<MessageSummary> getAllMessageSummaries(@QueryParam(START_TIME) String startTimeStr,
                                                       @QueryParam(END_TIME) String endTimeStr,
                                                       @QueryParam(USER) List<String> users,
                                                       @QueryParam(ROOM) List<String> rooms,
                                                       @QueryParam(MESSAGE_TYPE) String msgTypeStr) {

        LOG.debug("Got a call for msg summaries with starttime={} endtime={} users={} rooms={}",
                  startTimeStr, endTimeStr, users, rooms);

        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);
        users = ResourceUtils.getListFromNullable(users);
        rooms = ResourceUtils.getListFromNullable(rooms);

        Optional<String> optMessageType = ResourceUtils.getOptionalForParameter(msgTypeStr);
        if (optMessageType.isPresent()) {
            MessageType msgType = MessageType.fromType(optMessageType.get());
            return msgSummaryDao.getAllMessageSummariesForType(msgType, interval, rooms, users);
        } else {
            return msgSummaryDao.getAllMessageSummaries(interval, rooms, users);
        }
    }

    /**
     * Gets the total number of messages in a particular date range and/or user and/or room and/or
     * type. Note that the only required parameter is the time range
     *
     * @param startTimeStr
     *            The start time to get the summaries for
     * @param endTimeStr
     *            The end time to get the summaries for
     * @param users
     *            The user to query for (optional)
     * @param rooms
     *            The room to query for (optional)
     * @param msgTypeStr
     *            The type of the message. See {@link MessageType} for more info (optional)
     * @return Returns the total number of {@link MessageSummary}s based on the given parameters.
     */
    @GET
    @Path("total")
    @Produces(MediaType.APPLICATION_JSON)
    public int getTotalMessageSummaries(@QueryParam(START_TIME) String startTimeStr,
                                        @QueryParam(END_TIME) String endTimeStr,
                                        @QueryParam(USER) List<String> users,
                                        @QueryParam(ROOM) List<String> rooms,
                                        @QueryParam(MESSAGE_TYPE) String msgTypeStr) {

        LOG.debug("Got total message summaries with starttime={} endtime={} users={} rooms={}",
                  startTimeStr, endTimeStr, users, rooms);

        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);
        users = ResourceUtils.getListFromNullable(users);
        rooms = ResourceUtils.getListFromNullable(rooms);
        Optional<String> optMessageType = ResourceUtils.getOptionalForParameter(msgTypeStr);
        if (optMessageType.isPresent()) {
            MessageType msgType = MessageType.fromType(optMessageType.get());
            return msgSummaryDao.getTotalMessageSummariesForType(msgType, interval, rooms, users);
        } else {
            return msgSummaryDao.getTotalMessageSummaries(interval, rooms, users);
        }
    }

    /**
     * Gets the active users or rooms (or any other supported {@link DimensionType} using the
     * selected {@link ActiveMethod} with the given parameters
     *
     * @param startTimeStr
     *            The start time to get the summaries for
     * @param endTimeStr
     *            The end time to get the summaries for
     * @param dimensionStr
     *            The type to get activity for. See {@link DimensionType}
     * @param methodStr
     *            The method to use to compute activity. See {@link ActiveMethod}
     * @param topNStr
     *            The number of elements to return
     * @return The most active user or room (or any other supported {@link DimensionType}
     */
    @GET
    @Path("active")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Double> getActive(@QueryParam(START_TIME) String startTimeStr,
                                         @QueryParam(END_TIME) String endTimeStr,
                                         @QueryParam(DIM) String dimensionStr,
                                         @QueryParam(METHOD) String methodStr,
                                         @QueryParam(TOP_N) String topNStr) {

        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);
        DimensionType dimension = DimensionType.fromDimensionName(dimensionStr);
        ActiveMethod method = ActiveMethod.fromMethodName(methodStr);
        Optional<Integer> topN = ResourceUtils.getOptionalForParameterAsInt(topNStr);

        if (dimension == DimensionType.ROOM) {
            return msgSummaryDao.getActiveRoomsByMethod(interval, method, topN.or(MAX_RESULTS));
        } else if (dimension == DimensionType.USER) {
            return msgSummaryDao.getActiveUsersByMethod(interval, method, topN.or(MAX_RESULTS));
        } else {
            String formatMsg = "The dimension %s you provided is not supported. Pass in %s or %s";
            throw new UnsupportedOperationException(String.format(formatMsg, dimensionStr,
                                                                  DimensionType.ROOM,
                                                                  DimensionType.USER));
        }
    }
}
