package com.chatalytics.web.resources;

import com.chatalytics.compute.chat.dao.ChatAPIFactory;
import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.User;
import com.chatalytics.web.constant.WebConstants;
import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import static com.chatalytics.web.constant.WebConstants.USER;

/**
 * REST endpoint for ChatAlytics users
 */
@Path(UsersResource.USER_ENDPOINT)
public class UsersResource {

    public static final String USER_ENDPOINT = WebConstants.API_PATH + "users";
    private static final Logger LOG = LoggerFactory.getLogger(UsersResource.class);

    private final IChatApiDAO chatApiDao;

    public UsersResource(ChatAlyticsConfig config) {
        this(ChatAPIFactory.getChatApiDao(config));
    }

    @VisibleForTesting
    protected UsersResource(IChatApiDAO chatApiDao) {
        this.chatApiDao = chatApiDao;
    }

    /**
     * @return A map of mention names to User objects
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, User> getUsers() {
        LOG.debug("Got a call to get users");
        return chatApiDao.getUsers().values().stream()
                                             .collect(Collectors.toMap(User::getMentionName,
                                                                       Function.identity()));
    }

    /**
     * Returns a {@link User} with the given <code>username</code>
     *
     * @param username
     *            The username of the user to be returned
     * @return A {@link User} with the given <code>username</code>
     */
    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@QueryParam(USER) String username) {
        Map<String, User> users = getUsers();
        return users.get(username);
    }

    /**
     * @return A map of mention names to photo URLs
     */
    @GET
    @Path("photourls")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getUserPhotoURLs() {
        LOG.debug("Got a call to get user photo URLs");
        return chatApiDao.getUsers().values().stream()
                                             .filter(u -> u.getMentionName() != null
                                                          && u.getPhotoUrl() != null)
                                             .collect(Collectors.toMap(User::getMentionName,
                                                                       User::getPhotoUrl));
    }
}
