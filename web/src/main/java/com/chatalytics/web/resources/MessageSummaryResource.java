package com.chatalytics.web.resources;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IMessageSummaryDAO;
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import static com.chatalytics.web.constant.WebConstants.END_TIME;
import static com.chatalytics.web.constant.WebConstants.ROOM;
import static com.chatalytics.web.constant.WebConstants.START_TIME;
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

    private final IMessageSummaryDAO msgSummaryDao;
    private final DateTimeZone dtz;

    private static final Logger LOG = LoggerFactory.getLogger(MessageSummaryResource.class);

    public MessageSummaryResource(ChatAlyticsConfig config) {
        msgSummaryDao = ChatAlyticsDAOFactory.createMessageSummaryDAO(config);
        dtz = DateTimeZone.forID(config.timeZone);
     }

    /**
     * Gets the total number of messages in a particular date range and/or user and/or room and/or
     * type. Note that the only required parameter is the time range
     *
     * @param startTimeStr
     *            The start time to get the summaries for
     * @param endTimeStr
     *            The end time to get the summaries for
     * @param user
     *            The user to query for (optional)
     * @param room
     *            The room to query for (optional)
     * @param msgTypeStr
     *            The type of the message. See {@link MessageType} for more info (optional)
     * @return Returns the total number of message summaries.
     */
    @GET
    @Path("total")
    @Produces(MediaType.APPLICATION_JSON)
    public int getTotalMessageSummaries(@QueryParam(START_TIME) String startTimeStr,
                                        @QueryParam(END_TIME) String endTimeStr,
                                        @QueryParam(USER) String user,
                                        @QueryParam(ROOM) String room,
                                        @QueryParam(MESSAGE_TYPE) String msgTypeStr) {

        LOG.debug("Got a call for total msg summaries with starttime={} endtime={} user={} room={}",
                  startTimeStr, endTimeStr, user, room);

        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);
        Optional<String> username = ResourceUtils.getOptionalForParameter(user);
        Optional<String> roomName = ResourceUtils.getOptionalForParameter(room);
        Optional<String> optMessageType = ResourceUtils.getOptionalForParameter(msgTypeStr);
        if (optMessageType.isPresent()) {
            MessageType msgType = MessageType.fromType(optMessageType.get());
            return msgSummaryDao.getTotalMessageSummariesForType(msgType, interval, roomName,
                                                                 username);
        } else {
            return msgSummaryDao.getTotalMessageSummaries(interval, roomName, username);
        }
    }
}
