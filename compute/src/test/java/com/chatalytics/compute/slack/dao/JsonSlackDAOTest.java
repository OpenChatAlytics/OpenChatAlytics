package com.chatalytics.compute.slack.dao;

import com.chatalytics.compute.util.YamlUtils;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.Room;
import com.chatalytics.core.model.User;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Tests {@link JsonSlackDAO} by spying it and making it return a JSON value.
 *
 * @author giannis
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonSlackDAOTest {


    private JsonSlackDAO underTest;

    @Before
    public void setUp() throws Exception {
        ChatAlyticsConfig config = YamlUtils.readYamlFromResource("chatalytics.yaml",
                                                                  ChatAlyticsConfig.class);
        Client mockClient = mock(Client.class);
        WebResource mockResource = mock(WebResource.class);
        WebResource mockChanResource = mock(WebResource.class);
        WebResource mockUserResource = mock(WebResource.class);

        when(mockClient.resource(config.slackConfig.baseSlackURL)).thenReturn(mockResource);
        when(mockResource.path("channels.list")).thenReturn(mockChanResource);
        when(mockResource.path("users.list")).thenReturn(mockUserResource);

        Path channelsPath =
            Paths.get(Resources.getResource("slack_api_responses/channels.list.txt").toURI());
        String channelsResponseStr = new String(Files.readAllBytes(channelsPath));
        Path usersPath =
            Paths.get(Resources.getResource("slack_api_responses/users.list.txt").toURI());
        String usersResponseStr = new String(Files.readAllBytes(usersPath));

        underTest = spy(new JsonSlackDAO(config, mockClient));
        doReturn(channelsResponseStr).when(underTest).getJsonResultWithRetries(mockChanResource,
                                                                               config.apiRetries);
        doReturn(usersResponseStr).when(underTest).getJsonResultWithRetries(mockUserResource,
                                                                            config.apiRetries);
    }

    /**
     * Makes sure rooms can be returned
     */
    @Test
    public void testGetRooms() {
        Map<String, Room> rooms = underTest.getRooms();
        assertEquals(2, rooms.size());
    }

    /**
     * Makes sure users can be returned
     */
    @Test
    public void testGetUsers() {
        Map<String, User> users = underTest.getUsers();
        assertEquals(2, users.size());
    }

}
