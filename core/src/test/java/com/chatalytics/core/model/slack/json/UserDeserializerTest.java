package com.chatalytics.core.model.slack.json;

import com.chatalytics.core.model.data.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link UserDeserializer} for slack.
 *
 * @author giannis
 */
public class UserDeserializerTest {

    private ObjectMapper objMapper;

    @Before
    public void setUp() throws Exception {
        objMapper = new ObjectMapper();
        objMapper.registerModule(new SlackJsonModule());
    }

    /**
     * Tests to see if a user can be properly deserialized
     */
    @Test
    public void testDeserialize() throws Exception {
        User u = objMapper.readValue(userJsonStr, User.class);
        assertEquals("U023BECGF", u.getUserId());
        assertEquals("bobby@slack.com", u.getEmail());
        assertEquals("bobby", u.getMentionName());
        assertEquals("Bobby Tables", u.getName());
        assertEquals("https://test2.com", u.getPhotoUrl());
        assertEquals("America/New York/New York", u.getTimezone());
        assertEquals("some status", u.getStatusMessage());
        assertEquals("title", u.getTitle());
        assertTrue(u.isDeleted());
        assertTrue(u.isGroupAdmin());
        assertNull(u.getCreationDate());
        assertNull(u.getLastActiveDate());
        assertNull(u.getStatus());
    }

    private final String userJsonStr = "{" +
                                           "\"id\": \"U023BECGF\"," +
                                           "\"name\": \"bobby\"," +
                                           "\"deleted\": true," +
                                           "\"color\": \"9f69e7\"," +
                                           "\"tz\": \"America/New York/New York\"," +
                                           "\"status\": \"some status\"," +
                                           "\"profile\": {" +
                                               "\"first_name\": \"Bobby\"," +
                                               "\"last_name\": \"Tables\"," +
                                               "\"real_name\": \"Bobby Tables\"," +
                                               "\"title\": \"title\"," +
                                               "\"email\": \"bobby@slack.com\"," +
                                               "\"skype\": \"my-skype-name\"," +
                                               "\"phone\": \"+1 (123) 456 7890\"," +
                                               "\"image_24\": \"https:\\/\\/test1.com\"," +
                                               "\"image_32\": \"https:\\/\\/test2.com\"," +
                                               "\"image_48\": \"https:\\/\\/test3.com\"," +
                                               "\"image_72\": \"https:\\/\\/test4.com\"," +
                                               "\"image_192\": \"https:\\/\\/test5.com\"" +
                                           "}," +
                                           "\"is_admin\": true," +
                                           "\"is_owner\": true," +
                                           "\"has_2fa\": false," +
                                           "\"has_files\": true" +
                                       "}";

}
