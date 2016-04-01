package com.chatalytics.core.model.slack.json;

import com.chatalytics.core.model.Room;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link RoomDeserializer} for slack.
 *
 * @author giannis
 */
public class RoomDeserializerTest {

    private ObjectMapper objMapper;

    @Before
    public void setUp() throws Exception {
        objMapper = new ObjectMapper();
        objMapper.registerModule(new SlackJsonModule());
    }

    /**
     * Reads a JSON representation of a room and makes sure that all the fields are properly set.
     */
    @Test
    public void testDeserialize() throws Exception {
        Room r = objMapper.readValue(roomJsonStr, Room.class);
        assertEquals("C024BE91L", r.getRoomId());
        assertEquals(new DateTime(1360782804L * 1000), r.getCreationDate());
        assertEquals("fun", r.getName());
        assertEquals("U024BE7LH", r.getOwnerUserId());
        assertEquals("C024BE91L", r.getRoomId());
        assertEquals("Fun times", r.getTopic());
        assertTrue(r.isArchived());
        assertFalse(r.isPrivateRoom());
        assertNull(r.getXmppJid());
        assertNull(r.getGuestAccessURL());
        assertNull(r.getLastActiveDate());
    }

    private final String roomJsonStr = "{" +
                                           "\"id\": \"C024BE91L\"," +
                                           "\"name\": \"fun\"," +
                                           "\"created\": 1360782804," +
                                           "\"creator\": \"U024BE7LH\"," +
                                           "\"is_archived\": true," +
                                           "\"is_member\": false," +
                                           "\"num_members\": 6," +
                                           "\"topic\": {" +
                                               "\"value\": \"Fun times\"," +
                                               "\"creator\": \"U024BE7LV\"," +
                                               "\"last_set\": 1369677212" +
                                           "}," +
                                           "\"purpose\": {" +
                                               "\"value\": \"This channel is for fun\"," +
                                               "\"creator\": \"U024BE7LH\"," +
                                               "\"last_set\": 1360782804" +
                                           "}" +
                                       "},";

}
