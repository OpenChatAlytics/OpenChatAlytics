package com.chatalytics.compute.slack.dao;

import com.chatalytics.compute.chat.dao.AbstractJSONChatApiDAO;
import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.Message;
import com.chatalytics.core.model.Room;
import com.chatalytics.core.model.User;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

    private final WebResource resource;
    private final ChatAlyticsConfig config;
    private final ObjectMapper objMapper;

    private final DateTimeZone dtz;
    private final DateTimeFormatter apiDateFormat;

    public JsonSlackDAO(ChatAlyticsConfig config) {
        super(config.slackConfig.authTokens, AUTH_TOKEN_PARAM);
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        this.resource = client.resource(config.slackConfig.baseSlackURL);
        this.config = config;
        this.dtz = DateTimeZone.forID(config.timeZone);
        this.apiDateFormat = DateTimeFormat.forPattern(config.apiDateFormat).withZone(dtz);
        this.objMapper = new ObjectMapper();
    }

    @Override
    public Map<Integer, Room> getRooms() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Integer, User> getUsers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<Integer, User> getUsersForRoom(Room room) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Message> getMessages(DateTime start, DateTime end, Room room) {
        // TODO Auto-generated method stub
        return null;
    }

}
