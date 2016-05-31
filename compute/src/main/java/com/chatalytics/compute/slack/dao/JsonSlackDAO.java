package com.chatalytics.compute.slack.dao;

import com.chatalytics.compute.chat.dao.AbstractJSONChatApiDAO;
import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.exception.NotConnectedException;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.json.JsonObjectMapperFactory;
import com.chatalytics.core.model.data.Message;
import com.chatalytics.core.model.data.Room;
import com.chatalytics.core.model.data.User;
import com.chatalytics.core.model.slack.HistoryResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * JSON implementation of the {@link IChatApiDAO} for Slack
 *
 * @author giannis
 *
 */
public class JsonSlackDAO extends AbstractJSONChatApiDAO {

    private static final String AUTH_TOKEN_PARAM = "token";
    private static final Logger LOG = LoggerFactory.getLogger(JsonSlackDAO.class);

    private final WebResource resource;
    private final ObjectMapper objMapper;
    private final int apiRetries;

    public JsonSlackDAO(ChatAlyticsConfig config, Client client) {
        super(config.computeConfig.chatConfig.getAuthTokens(), AUTH_TOKEN_PARAM);
        this.resource = client.resource(config.computeConfig.chatConfig.getBaseAPIURL());
        this.apiRetries = config.computeConfig.apiRetries;
        this.objMapper = JsonObjectMapperFactory.createObjectMapper(config.inputType);
    }

    @Override
    public Map<String, Room> getRooms() {
        WebResource roomResource = resource.path("channels.list");
        String jsonStr = getJsonResultWithRetries(roomResource, apiRetries);
        Collection<Room> roomCol = deserializeJsonStr(jsonStr, "channels", Room.class, objMapper);
        Map<String, Room> result = Maps.newHashMapWithExpectedSize(roomCol.size());
        for (Room room : roomCol) {
            result.put(room.getRoomId(), room);
        }
        return result;
    }

    @Override
    public Map<String, User> getUsers() {
        WebResource userResource = resource.path("users.list");
        String jsonStr = getJsonResultWithRetries(userResource, apiRetries);
        Collection<User> userCol = deserializeJsonStr(jsonStr, "members", User.class, objMapper);
        Map<String, User> result = Maps.newHashMapWithExpectedSize(userCol.size());
        for (User user : userCol) {
            result.put(user.getUserId(), user);
        }
        return result;
    }

    @Override
    public Map<String, User> getUsersForRoom(Room room) {
        WebResource roomResource = resource.path("channels.info");
        roomResource = roomResource.queryParam("channel", room.getRoomId());
        String jsonStr = getJsonResultWithRetries(roomResource, apiRetries);
        Collection<String> userIdCol = deserializeJsonStr(jsonStr,
                                                          Lists.newArrayList("channel", "members"),
                                                          String.class,
                                                          objMapper);
        // get info for user IDs
        Map<String, User> result = Maps.newHashMapWithExpectedSize(userIdCol.size());
        for (String userId : userIdCol) {
            WebResource userResource = resource.path("users.info");
            userResource = userResource.queryParam("user", userId);
            jsonStr = getJsonResultWithRetries(userResource, apiRetries);
            try {
                JsonNode jsonNode = objMapper.readTree(jsonStr);
                jsonNode = jsonNode.get("user");
                User user = objMapper.readValue(jsonNode.toString(), User.class);
                result.put(user.getUserId(), user);
            } catch (IOException e) {
                throw new RuntimeException("Can't deserialize user with ID:" + userId, e);
            }
        }
        return result;
    }

    /**
     * @return A URI for initiating the realtime web socket connection
     */
    public URI getRealtimeWebSocketURI() {
        WebResource rtmResource = resource.path("rtm.start");
        String jsonStr = getJsonResultWithRetries(rtmResource, apiRetries);

        JsonNode tree;
        try {
            tree = objMapper.readTree(jsonStr);
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse realtime resource response", e);

        }
        boolean ok = tree.get("ok").asBoolean();
        if (!ok) {
            throw new NotConnectedException("Failed to connect to Slack API. Reason: "
                + tree.get("error").asText());
        }
        String webSocketUrl = tree.get("url").asText();
        return URI.create(webSocketUrl);

    }

    @Override
    public List<Message> getMessages(DateTime start, DateTime end, Room room) {
        WebResource historyResource = resource.path("channels.history");
        List<Message> result = Lists.newArrayList();
        boolean hasNext = true;

        String startMillisStr = formatDateTime(start);

        String endMillisStr = formatDateTime(end);

        while (hasNext) {

            historyResource = historyResource.queryParam("channel", room.getRoomId())
                                             .queryParam("latest", endMillisStr)
                                             .queryParam("oldest", startMillisStr)
                                             .queryParam("inclusive", "0")
                                             .queryParam("count", "1000");

            String jsonStr = getJsonResultWithRetries(historyResource, apiRetries);
            try {
                HistoryResult history = objMapper.readValue(jsonStr, HistoryResult.class);
                if (history.getMessages().isEmpty()) {
                    break;
                }
                result.addAll(history.getMessages());

                Comparator<Message> comp = (msg1, msg2) -> msg1.getDate().compareTo(msg2.getDate());
                DateTime earliestDate = history.getMessages().stream().min(comp).get().getDate();

                hasNext = history.isHas_more();
                endMillisStr = formatDateTime(earliestDate);
            } catch (IOException e) {
                LOG.error("Can't deserialize history", e);
                return ImmutableList.of();
            }
        }
        return result;
    }

    /**
     * Helper method for deserializing lists of elements of type <code>T</code> from a response JSON
     * string
     *
     * @param jsonStr
     *            The API returned JSON string
     * @param listElement
     *            The element in the original response JSON string that contains the list of items
     *            of type <code>T</code> to deserialize
     * @param clazz
     *            The class to deserialize
     * @param objMapper
     *            The JSON object mapper used to deserialize the JSON string.
     * @return A collection of elements of type <code>clazz</code>.
     */
    private <T> Collection<T> deserializeJsonStr(String jsonStr, String listElement,
                                                 Class<T> clazz,
                                                 ObjectMapper objMapper) {
        return deserializeJsonStr(jsonStr, Lists.newArrayList(listElement), clazz, objMapper);
    }

    /**
     * Helper method for deserializing lists of elements of type <code>T</code> from a response JSON
     * string
     *
     * @param jsonStr
     *            The API returned JSON string
     * @param listElements
     *            A list of ordered elements in the original response JSON to traverse down to to
     *            get to the element that contains the list of items of type <code>T</code> to
     *            deserialize
     *            <p/>
     *            For example given the following JSON string
     *
     * <pre>
     *            {
     *                "someElement": {
     *                    "someInnerElement": [
     *                        {...},
     *                        {...}
     *                    ],
     *                    "otherElement": "value"
     *                }
     *            }
     * </pre>
     *
     *            If the collection of <code>T</code>s we were interested in were inside
     *            <code>someInnerEllement</code> then the list would equal:
     *            <code>[someElement, someInnterElement]</code>
     * @param clazz
     *            The class to deserialize The JSON object mapper used to deserialize the JSON
     *            string.
     * @return A collection of elements of type <code>clazz</code>.
     */
    private <T> Collection<T> deserializeJsonStr(String jsonStr, List<String> listElements,
                                                 Class<T> clazz,
                                                 ObjectMapper objMapper) {
        TypeFactory typeFactory = objMapper.getTypeFactory();
        CollectionType type = typeFactory.constructCollectionType(List.class, clazz);
        try {
            JsonNode jsonNode = objMapper.readTree(jsonStr);
            // traverse down the order of elements until the desired one is reached
            for (String jsonEl : listElements) {
                jsonNode = jsonNode.get(jsonEl);
                if (jsonNode == null) {
                    return ImmutableList.of();
                }
            }
            return objMapper.readValue(jsonNode.toString(), type);
        } catch (IOException e) {
            LOG.error("Got exception when trying to deserialize list of {}", clazz, e);
            return ImmutableList.of();
        }
    }

    private String formatDateTime(DateTime date) {
        long millis = date.getMillis();
        return String.format("%s.%s", String.valueOf(millis / 1000), millis % 1000);
    }
}
