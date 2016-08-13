package com.chatalytics.web.resources;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.core.model.data.Room;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests RoomsResource
 *
 * @author psastras
 */
public class RoomsResourceTest {

    private RoomsResource underTest;
    private IChatApiDAO chatApiDao;

    @Before
    public void setUp() {
        chatApiDao = mock(IChatApiDAO.class);
        underTest = new RoomsResource(chatApiDao);
    }

    @Test
    public void testGetRooms() {
        Room room1 = new Room("r1", "r1channel", "r1topic", null ,null, "owner1", false, false,
                              null, null);
        Room room2 = new Room("r2", "r2channel", "r2topic", null ,null, "owner2", false, false,
                              null, null);
        Map<String, Room> rooms = ImmutableMap.of("r1", room1, "r2", room2);
        when(chatApiDao.getRooms()).thenReturn(rooms);
        Map<String, Room> expectedResult = ImmutableMap.of(room1.getName(), room1,
                                                           room2.getName(), room2);

        Map<String, Room> result = underTest.getRooms();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testGetRoom() {
        Room room1 = new Room("r1", "r1channel", "r1topic", null ,null, "owner1", false, false,
                              null, null);
        Room room2 = new Room("r2", "r2channel", "r2topic", null ,null, "owner2", false, false,
                              null, null);
        Map<String, Room> rooms = ImmutableMap.of("r1", room1, "r2", room2);
        when(chatApiDao.getRooms()).thenReturn(rooms);

        Room result = underTest.getRoom("r1channel");
        assertEquals(rooms.get("r1"), result);
    }
}
