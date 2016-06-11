package com.chatalytics.compute.chat.dao.local;

import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.LocalTestConfig;
import com.chatalytics.core.model.data.Room;
import com.chatalytics.core.model.data.User;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link LocalChatDao}
 *
 * @author giannis
 */
public class LocalChatDaoTest {

    private ChatAlyticsConfig config;
    private LocalChatDao underTest;
    private LocalTestConfig chatConfig;

    @Before
    public void setUp() {
        config = new ChatAlyticsConfig();
        chatConfig = new LocalTestConfig();
        chatConfig.randomSeed = 0L;
        config.computeConfig.chatConfig = chatConfig;

        underTest = new LocalChatDao(config);
    }

    @Test
    public void testInit_withoutRandomSeed() {
        chatConfig.randomSeed = null;
        underTest = new LocalChatDao(config);
        assertNotNull(underTest.getUsers());
        assertNotNull(underTest.getRooms());
    }

    @Test
    public void testGetRooms() {
        Map<String, Room> rooms = underTest.getRooms();
        assertNotNull(rooms);
        assertFalse(rooms.isEmpty());
    }

    @Test
    public void testGetUsers() {
        Map<String, User> users = underTest.getUsers();
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    public void testGetUsersForRoom() {
        Map<String, User> expected = underTest.getUsers();
        Map<String, User> result = underTest.getUsersForRoom(mock(Room.class));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(expected, result);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void testGetMessages() {
        underTest.getMessages(DateTime.now(), DateTime.now(), mock(Room.class));
    }

    @Test
    public void testGetEmojis() {
        Map<String, String> result = underTest.getEmojis();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
