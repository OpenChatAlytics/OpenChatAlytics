package com.chatalytics.compute.hipchat.dao;

import com.chatalytics.compute.chat.dao.AbstractJSONChatApiDAO;
import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.Message;
import com.chatalytics.core.model.Room;
import com.chatalytics.core.model.User;
import com.chatalytics.core.model.hipchat.json.HipChatJsonModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import org.apache.commons.lang.NotImplementedException;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * JSON implementation of the {@link IChatApiDAO} for HipChat
 *
 * @author giannis
 *
 */
public class JsonHipChatDAO extends AbstractJSONChatApiDAO {

    private static final String AUTH_TOKEN_PARAM = "auth_token";

    private final WebResource resource;
    private final ChatAlyticsConfig config;
    private final ObjectMapper objMapper;

    public final DateTimeZone dtz;
    public final DateTimeFormatter apiDateFormat;

    public JsonHipChatDAO(ChatAlyticsConfig config) {
        super(config.hipchatConfig.authTokens, AUTH_TOKEN_PARAM);
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        this.resource = client.resource(config.hipchatConfig.baseHipChatURL);
        this.config = config;
        this.dtz = DateTimeZone.forID(config.timeZone);
        this.apiDateFormat = DateTimeFormat.forPattern(config.apiDateFormat).withZone(dtz);
        this.objMapper = new ObjectMapper();
        this.objMapper.registerModule(new HipChatJsonModule());
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
}
