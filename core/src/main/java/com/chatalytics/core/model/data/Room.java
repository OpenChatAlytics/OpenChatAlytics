package com.chatalytics.core.model.data;

import org.joda.time.DateTime;

import lombok.Data;

/**
 * Represents a chat room.
 *
 * @author giannis
 *
 */
@Data
public class Room {

    private final String roomId;
    private final String name;
    private final String topic;
    private final DateTime lastActiveDate;
    private final DateTime creationDate;
    private final String ownerUserId;
    private final boolean archived;
    private final boolean privateRoom;
    private final String guestAccessURL;
    private final String xmppJid;


}
