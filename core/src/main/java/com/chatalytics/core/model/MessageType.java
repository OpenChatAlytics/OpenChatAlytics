package com.chatalytics.core.model;

import com.chatalytics.core.similarity.SimilarityDimension;
import com.google.common.base.Preconditions;

import java.util.Arrays;

/**
 * All the different types of messages supported by ChatAlytics
 *
 * @author giannis
 */
public enum MessageType {

    MESSAGE("message"),
    CHANNEL_JOIN("channel_join"),
    MESSAGE_CHANGED("message_changed"),
    BOT_MESSAGE("bot_message"),
    PINNED_ITEM("pinned_item"),
    UNKNOWN("unknown");

    private String type;

    private MessageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }

    /**
     * Constructs a <code>MessageType</code> from its string representation.
     *
     * @param value
     *            The string type of a <code>MessageType</code>
     * @return A <code>MessageType</code>
     * @throws IllegalArgumentException
     *             when the type cannot be determined or if <code>value</code> is null
     * @throws NullPointerException if <code>type</code> is null
     */
    public static MessageType fromType(String type) {
        Preconditions.checkNotNull(type, "Can't construct a message type from a null string type");

        for (MessageType msgType : MessageType.values()) {
            if (type.equals(msgType.getType())) {
                return msgType;
            }
        }

        String msg = String.format("Can't construct message type from %s. Supported values are %s",
                                   type, Arrays.deepToString(SimilarityDimension.values()));
        throw new IllegalArgumentException(msg);
    }

    /**
     * Similar to {@link #fromType(String)} but instead of failing on identifying a type it returns
     * {@link #UNKNOWN} instead
     *
     * @param type
     *            The string type of a <code>MessageType</code>
     * @return The identified <code>MessageType</code> or {@link #UNKNOWN} if it can't be determined
     * @throws NullPointerException
     *             if <code>type</code> is null
     */
    public static MessageType fromTypeOrUnknown(String type) {
        try {
            return MessageType.fromType(type);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
