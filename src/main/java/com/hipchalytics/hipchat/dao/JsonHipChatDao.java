package com.hipchalytics.hipchat.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hipchalytics.config.HipChalyticsConfig;
import com.hipchalytics.model.Message;
import com.hipchalytics.model.Room;
import com.hipchalytics.model.User;
import com.hipchalytics.model.json.HipChalyticsJsonModule;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import org.apache.commons.lang.NotImplementedException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.core.MediaType;

/**
 * JSON implementation of the {@link IHipChatApiDao}.
 *
 * @author giannis
 *
 */
public class JsonHipChatDao implements IHipChatApiDao {

    private final WebResource resource;
    private final HipChalyticsConfig config;
    private final Random rand;
    private final ObjectMapper objMapper;

    public final DateTimeZone dtz;
    public final DateTimeFormatter apiDateFormat;

    private static final String AUTH_TOKEN_PARAM = "auth_token";
    private static final Logger LOG = LoggerFactory.getLogger(JsonHipChatDao.class);

    public JsonHipChatDao(HipChalyticsConfig config) {
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        this.resource = client.resource(config.baseHipChatURL);
        this.config = config;
        this.rand = new Random(System.currentTimeMillis());
        this.dtz = DateTimeZone.forID(config.timeZone);
        this.apiDateFormat = DateTimeFormat.forPattern(config.apiDateFormat).withZone(dtz);
        this.objMapper = new ObjectMapper();
        objMapper.registerModule(new HipChalyticsJsonModule());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, Room> getRooms() {
        WebResource roomResource = resource.path("rooms/list");
        String jsonStr = getJsonResultWithRetries(roomResource, config.apiRetries);
        Collection<Room> roomCol = deserializeJsonStr(jsonStr, "rooms", Room.class, objMapper);
        Map<Integer, Room> result = Maps.newHashMapWithExpectedSize(roomCol.size());
        for (Room room : roomCol) {
            result.put(room.getRoomId(), room);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, User> getUsers() {
        WebResource userResource = resource.path("users/list");
        String jsonStr = getJsonResultWithRetries(userResource, config.apiRetries);
        Collection<User> userCol = deserializeJsonStr(jsonStr, "users", User.class, objMapper);
        Map<Integer, User> result = Maps.newHashMapWithExpectedSize(userCol.size());
        for (User user : userCol) {
            result.put(user.getUserId(), user);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, User> getUsersForRoom(Room room) {
        throw new NotImplementedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> getMessages(DateTime start, DateTime end, Room room) {
        DateTime curDate = start;
        List<Message> messages = Lists.newArrayList();
        WebResource roomsResource = resource.path("rooms/history");
        roomsResource = roomsResource.queryParam("room_id", String.valueOf(room.getRoomId()))
            .queryParam("timezone", config.timeZone);
        Interval messageInterval = new Interval(start, end);
        while (curDate.isBefore(end) || curDate.equals(end)) {
            roomsResource = roomsResource.queryParam("date", curDate.toString(apiDateFormat));
            String jsonStr = getJsonResultWithRetries(roomsResource, config.apiRetries);
            Collection<Message> messageCol = deserializeJsonStr(jsonStr, "messages", Message.class,
                                                                objMapper);
            for (Message message : messageCol) {
                if (messageInterval.contains(message.getDate())) {
                    messages.add(message);
                }
            }
            curDate = curDate.plusDays(1);
        }
        return messages;
    }

    /**
     * Helper method for doing GETs with <code>retries</code> number of retries in case of 403
     * errors.
     *
     * @param resource
     *            The resource to GET data from
     * @param retries
     *            The number of retries if a 403 is encountered.
     * @return The JSON result string.
     */
    private String getJsonResultWithRetries(WebResource resource, int retries) {
        resource = addTokenQueryParam(resource);
        while (retries >= 0) {
            try {
                String jsonStr = resource.accept(MediaType.APPLICATION_JSON).get(String.class);
                return jsonStr;
            } catch (UniformInterfaceException e) {
                if (e.getResponse().getStatus() == Status.FORBIDDEN.getStatusCode()) {
                    retries--;
                }
            }
        }
        return "{}";
    }

    /**
     * Helper method for adding the token query parameter.
     *
     * @param resource
     *            The resource to add the token parameter to.
     * @return Returns a new resource with the token query parameter added.
     */
    private WebResource addTokenQueryParam(WebResource resource) {
        int tokenSize = config.authTokens.size();
        String randomAuthToken = config.authTokens.get(rand.nextInt(tokenSize));
        return resource.queryParam(AUTH_TOKEN_PARAM, randomAuthToken);
    }

    /**
     * Helper method for deserializing a hipchat JSON response to a collection of objects.
     *
     * @param jsonStr
     *            The hipchat JSON response.
     * @param mapElement
     *            Hipchat JSON responses are actually maps with a single element. This argument is
     *            the value of the element to pull out from the map.
     * @param colClassElements
     *            The types of objects that the collection object will contain.
     * @param objMapper
     *            The JSON object mapper used to deserialize the JSON string.
     * @return A collection of elements of type <code>colClassElements</code>.
     */
    private <T> Collection<T> deserializeJsonStr(String jsonStr, String mapElement,
            Class<T> colClassElements, ObjectMapper objMapper) {
        Map<String, Collection<T>> re;
        try {
            TypeFactory typeFactory = objMapper.getTypeFactory();
            CollectionType type = typeFactory.constructCollectionType(List.class, colClassElements);
            MapType thetype = typeFactory.constructMapType(HashMap.class,
                                                           typeFactory.constructType(String.class),
                                                           type);
            re = objMapper.readValue(jsonStr, thetype);
        } catch (IOException e) {
            LOG.error("Got {} when trying to deserialize list of {}", colClassElements,
                      e.getMessage(), e);
            return Lists.newArrayListWithExpectedSize(0);
        }
        return re.get(mapElement);
    }
}
