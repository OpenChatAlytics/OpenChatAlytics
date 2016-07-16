package com.chatalytics.web.resources;

import com.chatalytics.compute.chat.dao.ChatAPIFactory;
import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IEmojiDAO;
import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.DimensionType;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.emoji.LocalEmojiUtils;
import com.chatalytics.core.json.JsonObjectMapperFactory;
import com.chatalytics.core.model.data.EmojiEntity;
import com.chatalytics.core.model.data.EmojiMap;
import com.chatalytics.web.constant.WebConstants;
import com.chatalytics.web.utils.DateTimeUtils;
import com.chatalytics.web.utils.ResourceUtils;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
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

import static com.chatalytics.web.constant.WebConstants.BOT;
import static com.chatalytics.web.constant.WebConstants.END_TIME;
import static com.chatalytics.web.constant.WebConstants.ROOM;
import static com.chatalytics.web.constant.WebConstants.START_TIME;
import static com.chatalytics.web.constant.WebConstants.TOP_N;
import static com.chatalytics.web.constant.WebConstants.USER;

/**
 * REST endpoint for getting top emojis collected from chat messages
 *
 * @author giannis
 *
 */
@Path(EmojisResource.EMOJI_ENDPOINT)
public class EmojisResource {

    public static final String EMOJI_ENDPOINT = WebConstants.API_PATH + "emojis";
    public static final String FIRST_SIMILARITY_DIM = "firstDim";
    public static final String SECOND_SIMILARITY_DIM = "secondDim";
    public static final String DIM = "dimension";
    public static final String METHOD = "method";

    private static final int MAX_RESULTS = 20;
    private static final Logger LOG = LoggerFactory.getLogger(EmojisResource.class);

    private final IEmojiDAO emojiDao;
    private final DateTimeZone dtz;
    private final IChatApiDAO chatApiDao;
    private final Map<String, String> unicodeEmojis;

    public EmojisResource(ChatAlyticsConfig config) {
        this(config, ChatAPIFactory.getChatApiDao(config));
    }

    @VisibleForTesting
    protected EmojisResource(ChatAlyticsConfig config, IChatApiDAO chatApiDao) {
        emojiDao = ChatAlyticsDAOFactory.createEmojiDAO(config);
        this.chatApiDao = chatApiDao;
        dtz = DateTimeZone.forID(config.timeZone);
        ObjectMapper objectMapper = JsonObjectMapperFactory.createObjectMapper(config.inputType);
        unicodeEmojis = LocalEmojiUtils.getUnicodeEmojis(objectMapper);
    }

    @GET
    @Path("trending")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Long> getTopEmojis(@QueryParam(START_TIME) String startTimeStr,
                                          @QueryParam(END_TIME) String endTimeStr,
                                          @QueryParam(USER) List<String> users,
                                          @QueryParam(ROOM) List<String> rooms,
                                          @QueryParam(TOP_N) String topNStr,
                                          @QueryParam(BOT) String botStr)
                    throws JsonGenerationException, JsonMappingException, IOException {

        LOG.debug("Got query for starttime={} endtime={} users={} rooms={} botStr={}",
                  startTimeStr, endTimeStr, users, rooms, botStr);

        users = ResourceUtils.getListFromNullable(users);
        rooms = ResourceUtils.getListFromNullable(rooms);
        Optional<Integer> topN = ResourceUtils.getOptionalForParameterAsInt(topNStr);
        boolean withBots = ResourceUtils.getOptionalForParameterAsBool(botStr).or(false);

        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);

        return emojiDao.getTopEmojis(interval, rooms, users, topN.or(MAX_RESULTS), withBots);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<EmojiEntity> getAllEmojis(@QueryParam(START_TIME) String startTimeStr,
                                          @QueryParam(END_TIME) String endTimeStr,
                                          @QueryParam(USER) List<String> users,
                                          @QueryParam(ROOM) List<String> rooms,
                                          @QueryParam(BOT) String botStr) {

        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);
        users = ResourceUtils.getListFromNullable(users);
        rooms = ResourceUtils.getListFromNullable(rooms);
        boolean withBots = ResourceUtils.getOptionalForParameterAsBool(botStr).or(false);

        return emojiDao.getAllMentions(interval, rooms, users, withBots);
    }

    @GET
    @Path("similarities")
    @Produces(MediaType.APPLICATION_JSON)
    public LabeledDenseMatrix<String> getSimilarities(
            @QueryParam(START_TIME) String startTimeStr,
            @QueryParam(END_TIME) String endTimeStr,
            @QueryParam(FIRST_SIMILARITY_DIM) String firstDimStr,
            @QueryParam(SECOND_SIMILARITY_DIM) String secondDimStr,
            @QueryParam(BOT) String botStr) {

        LOG.debug("Got a call for dimensions {} and {} with starttime={} endtime={} botStr={}",
                  firstDimStr, secondDimStr, startTimeStr, endTimeStr, botStr);

        DimensionType firstDim = DimensionType.fromDimensionName(firstDimStr);
        DimensionType secondDim = DimensionType.fromDimensionName(secondDimStr);
        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);
        boolean withBots = ResourceUtils.getOptionalForParameterAsBool(botStr).or(false);

        if (firstDim == DimensionType.ROOM && secondDim == DimensionType.EMOJI) {
            return emojiDao.getRoomSimilaritiesByEmoji(interval, withBots);
        } else if (firstDim == DimensionType.USER && secondDim == DimensionType.EMOJI) {
            return emojiDao.getUserSimilaritiesByEmoji(interval, withBots);
        } else {
            String formatStr = "The dimension combination you specified (%s, %s) is not supported";
            throw new UnsupportedOperationException(String.format(formatStr, firstDimStr,
                                                                  secondDimStr));
        }
    }

    @GET
    @Path("active")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Double> getActive(@QueryParam(START_TIME) String startTimeStr,
                                         @QueryParam(END_TIME) String endTimeStr,
                                         @QueryParam(DIM) String dimensionStr,
                                         @QueryParam(METHOD) String methodStr,
                                         @QueryParam(TOP_N) String topNStr,
                                         @QueryParam(BOT) String botStr) {

        Interval interval = DateTimeUtils.getIntervalFromParameters(startTimeStr, endTimeStr, dtz);
        DimensionType dimension = DimensionType.fromDimensionName(dimensionStr);
        ActiveMethod method = ActiveMethod.fromMethodName(methodStr);
        int topN = ResourceUtils.getOptionalForParameterAsInt(topNStr).or(MAX_RESULTS);
        boolean withBots = ResourceUtils.getOptionalForParameterAsBool(botStr).or(false);

        if (dimension == DimensionType.ROOM) {
            return emojiDao.getActiveRoomsByMethod(interval, method, topN, withBots);
        } else if (dimension == DimensionType.USER) {
            return emojiDao.getActiveUsersByMethod(interval, method, topN, withBots);
        } else {
            String formatMsg = "The dimension %s you provided is not supported. Pass in %s or %s";
            throw new UnsupportedOperationException(String.format(formatMsg, dimensionStr,
                                                                  DimensionType.ROOM,
                                                                  DimensionType.USER));
        }
    }

    @GET
    @Path("icons")
    @Produces(MediaType.APPLICATION_JSON)
    public EmojiMap getEmojiIcons() {
        LOG.debug("Got request for icons");
        return new EmojiMap(chatApiDao.getEmojis(), unicodeEmojis);
    }

}
