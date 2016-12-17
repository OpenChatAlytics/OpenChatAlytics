package com.chatalytics.core.model.data;

import org.joda.time.DateTime;

import lombok.Data;

/**
 * Bean that represents a chat message with <code>int</code>s for the user and room.
 *
 * @author giannis
 *
 */
@Data
public class Message {

    private final DateTime date;
    private final String fromName;
    private final String fromUserId;
    private final String message;
    private final String roomId;
    private final MessageType type;

}
