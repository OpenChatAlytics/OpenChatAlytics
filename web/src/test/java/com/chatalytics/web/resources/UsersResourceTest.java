package com.chatalytics.web.resources;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.core.model.data.User;
import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests UsersResource
 *
 * @author giannis
 */
public class UsersResourceTest {

    private UsersResource underTest;
    private IChatApiDAO chatApiDao;

    @Before
    public void setUp() {
        chatApiDao = mock(IChatApiDAO.class);
        underTest = new UsersResource(chatApiDao);
    }

    @Test
    public void testGetUsers() {
        String url1 = "http://u1.com";
        String url2 = "http://u1.com";
        User user1 = new User("u1", "u1@email.com", false, false, false, "name", "mu1", url1,
                              DateTime.now(), DateTime.now(), null, null, null, null);
        User user2 = new User("u2", "u2@email.com", false, false, false, "name", "mu2", url2,
                              DateTime.now(), DateTime.now(), null, null, null, null);

        Map<String, User> users = ImmutableMap.of("id1", user1, "id2", user2);
        when(chatApiDao.getUsers()).thenReturn(users);
        Map<String, User> expectedResult = ImmutableMap.of(user1.getMentionName(), user1,
                                                           user2.getMentionName(), user2);

        Map<String, User> result = underTest.getUsers();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetUserPhotoURLs() {
        String url1 = "http://u1.com";
        String url2 = "http://u1.com";
        User user1 = new User("u1", "u1@email.com", false, false, false, "name", "mu1", url1,
                              DateTime.now(), DateTime.now(), null, null, null, null);
        User user2 = new User("u2", "u2@email.com", false, false, false, "name", "mu2", url2,
                              DateTime.now(), DateTime.now(), null, null, null, null);

        Map<String, User> users = ImmutableMap.of("id1", user1, "id2", user2);
        when(chatApiDao.getUsers()).thenReturn(users);
        Map<String, String> expectedResult = ImmutableMap.of(user1.getMentionName(), url1,
                                                             user2.getMentionName(), url2);

        Map<String, String> result = underTest.getUserPhotoURLs();
        assertEquals(expectedResult, result);
    }
}
