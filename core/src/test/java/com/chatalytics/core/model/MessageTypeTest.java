package com.chatalytics.core.model;

import com.chatalytics.core.model.data.MessageType;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link MessageType}
 *
 * @author giannis
 *
 */
public class MessageTypeTest {

    @Test
    public void testFromType() {

        String type = "message";
        MessageType msgType = MessageType.fromType(type);
        assertEquals(MessageType.MESSAGE, msgType);
        assertEquals(type, msgType.getType());
        assertEquals(type, msgType.toString());

        type = "channel_join";
        msgType = MessageType.fromType(type);
        assertEquals(MessageType.CHANNEL_JOIN, msgType);
        assertEquals(type, msgType.getType());
        assertEquals(type, msgType.toString());

        type = "message_changed";
        msgType = MessageType.fromType(type);
        assertEquals(MessageType.MESSAGE_CHANGED, msgType);
        assertEquals(type, msgType.getType());
        assertEquals(type, msgType.toString());

        type = "bot_message";
        msgType = MessageType.fromType(type);
        assertEquals(MessageType.BOT_MESSAGE, msgType);
        assertEquals(type, msgType.getType());
        assertEquals(type, msgType.toString());

        type = "pinned_item";
        msgType = MessageType.fromType(type);
        assertEquals(MessageType.PINNED_ITEM, msgType);
        assertEquals(type, msgType.getType());
        assertEquals(type, msgType.toString());

        type = "unknown";
        msgType = MessageType.fromType(type);
        assertEquals(MessageType.UNKNOWN, msgType);
        assertEquals(type, msgType.getType());
        assertEquals(type, msgType.toString());
    }

    @Test
    public void testFromTypeOrUnknown() {
        MessageType msgType = MessageType.fromTypeOrUnknown("bad name");
        assertEquals(MessageType.UNKNOWN, msgType);

        msgType = MessageType.fromTypeOrUnknown("message");
        assertEquals(MessageType.MESSAGE, msgType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromType_withBadName() {
        MessageType.fromType("bad name");
    }

    @Test(expected = NullPointerException.class)
    public void testFromType_withNullArg() {
        MessageType.fromType(null);
    }
}
