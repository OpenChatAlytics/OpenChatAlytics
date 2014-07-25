package com.hipchalytics.storm.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hipchalytics.config.HipChalyticsConfig;
import com.hipchalytics.model.Message;
import com.hipchalytics.model.Room;
import com.hipchalytics.model.User;
import com.hipchalytics.model.json.HipChalyticsJsonModule;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import org.apache.commons.lang.NotImplementedException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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

    public static final String TIMEZONE_ID = "America/New_York";
    public static final DateTimeZone dtz = DateTimeZone.forID(TIMEZONE_ID);
    public static final DateTimeFormatter API_DATE_FORMAT =
        DateTimeFormat.forPattern("YYYY-MM-dd").withZone(dtz);

    private static final String AUTH_TOKEN_PARAM = "auth_token";
    private static final Logger LOG = LoggerFactory.getLogger(JsonHipChatDao.class);

    public JsonHipChatDao(HipChalyticsConfig config) {
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        this.resource = client.resource(config.baseHipChatURL);
        this.config = config;
        this.rand = new Random(System.currentTimeMillis());
        this.objMapper = new ObjectMapper();
        objMapper.registerModule(new HipChalyticsJsonModule());
        // getMessages(new DateTime().minus(Days.FIVE), new DateTime(), new Room(150109, null, null,
        // null, null, 0, false,
        // false, null, null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, Room> getRooms() {
        WebResource roomResource = resource.path("rooms/list");
        roomResource = addTokenQueryParam(roomResource);
        String jsonStr = roomResource.accept(MediaType.APPLICATION_JSON).get(String.class);
        Collection<Room> roomCol = deserializeJsonStr(jsonStr, "rooms", Room.class, objMapper);
        Map<Integer, Room> result = Maps.newHashMapWithExpectedSize(roomCol.size());
        for (Room room : roomCol) {
            result.put(room.getRoomId(), room);
        }
        System.out.println(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, User> getUsers() {
        WebResource userResource = resource.path("users/list");
        userResource = addTokenQueryParam(userResource);
        String jsonStr = userResource.accept(MediaType.APPLICATION_JSON).get(String.class);
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
        WebResource userResource = resource.path("rooms/history");
        userResource = addTokenQueryParam(userResource)
            .queryParam("room_id", String.valueOf(room.getRoomId()))
            .queryParam("timezone", TIMEZONE_ID);
        while (curDate.isBefore(end) || curDate.equals(end)) {
            userResource = userResource.queryParam("date", curDate.toString(API_DATE_FORMAT));
            String jsonStr = userResource.get(String.class);
            Collection<Message> userCol = deserializeJsonStr(jsonStr, "messages", Message.class,
                                                             objMapper);
            for (Message message : userCol) {
                message.setRoomId(room.getRoomId());
                messages.add(message);
            }
            curDate = curDate.plusDays(1);
        }
        return messages;
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
