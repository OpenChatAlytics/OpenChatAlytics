package com.chatalytics.compute.storm.bolt;

import com.chatalytics.core.model.EmojiEntity;
import com.chatalytics.core.model.FatMessage;
import com.chatalytics.core.model.Message;
import com.chatalytics.core.model.MessageType;
import com.chatalytics.core.model.Room;
import com.chatalytics.core.model.User;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link EmojiCounterBolt}
 * @author giannis
 *
 */
public class EmojiCounterBoltTest {

    private EmojiCounterBolt undertest;
    private User user;
    private Room room;
    private DateTime mentionTime;
    private String emoji;

    @Before
    public void setUp() {
        undertest = new EmojiCounterBolt();
        String username = "randomUserName";
        String roomName = "randomRoomName";
        this.mentionTime = DateTime.now();
        this.emoji = "emoji";

        this.user = new User("randomUserId", "email", false, false, false, null, username, null, null,
                             null, null, null, null, null);
        this.room = new Room("randomRoomId", roomName, null, null, null, null, false, false, null,
                             null);

    }

    @Test
    public void testGetEmojisFromMessage() {
        Message message = new Message(mentionTime, "randomFrom", "randomUserId",
                                      String.format("test message with :%s:", emoji),
                                      "randomRoomId", MessageType.MESSAGE);

        FatMessage fatMessage = new FatMessage(message, user, room);

        List<EmojiEntity> emojis = undertest.getEmojisFromMessage(fatMessage);

        assertEquals(1, emojis.size());
        EmojiEntity firstEmoji = emojis.get(0);
        assertEquals(user.getMentionName(), firstEmoji.getUsername());
        assertEquals(room.getName(), firstEmoji.getRoomName());
        assertEquals(mentionTime, firstEmoji.getMentionTime());
        assertEquals(emoji, firstEmoji.getValue());
        assertEquals(1, firstEmoji.getOccurrences());
    }

    /**
     * Tests to see if multiple occurrences in the same message of the same emoji are correctly
     * recorded
     */
    @Test
    public void testGetEmojisFromMessage_withMultipleOccurrences() {
        Message message = new Message(mentionTime, "randomFrom", "randomUserId",
                                      String.format("test message with :%s::%s: test :%s:",
                                                    emoji, emoji, emoji),
                                      "randomRoomId", MessageType.MESSAGE);

        FatMessage fatMessage = new FatMessage(message, user, room);

        List<EmojiEntity> emojis = undertest.getEmojisFromMessage(fatMessage);

        assertEquals(1, emojis.size());
        EmojiEntity firstEmoji = emojis.get(0);
        assertEquals(user.getMentionName(), firstEmoji.getUsername());
        assertEquals(room.getName(), firstEmoji.getRoomName());
        assertEquals(mentionTime, firstEmoji.getMentionTime());
        assertEquals(emoji, firstEmoji.getValue());
        assertEquals(3, firstEmoji.getOccurrences());
    }

    @Test
    public void testGetEmojisFromMessage_withDanglingColons() {
        Message message = new Message(mentionTime, "randomFrom", "randomUserId",
                                      String.format("test http:// message :     testwith :%s::%s: "
                                          + "test :%s:", emoji, emoji, emoji),
                                      "randomRoomId", MessageType.MESSAGE);

        FatMessage fatMessage = new FatMessage(message, user, room);

        List<EmojiEntity> emojis = undertest.getEmojisFromMessage(fatMessage);

        assertEquals(1, emojis.size());
        EmojiEntity firstEmoji = emojis.get(0);
        assertEquals(user.getMentionName(), firstEmoji.getUsername());
        assertEquals(room.getName(), firstEmoji.getRoomName());
        assertEquals(mentionTime, firstEmoji.getMentionTime());
        assertEquals(emoji, firstEmoji.getValue());
        assertEquals(3, firstEmoji.getOccurrences());
    }

}