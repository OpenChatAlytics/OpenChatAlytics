package com.hipchalytics.model;

import com.google.common.base.MoreObjects;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Represents a hipchat room.
 *
 * @author giannis
 *
 */
public class Room implements Serializable {

    private final int roomId;
    private final String name;
    private final String topic;
    private final DateTime lastActiveDate;
    private final DateTime creationDate;
    private final int ownerUserId;
    private final boolean archived;
    private final boolean privateRoom;
    private final String guestAccessURL;
    private final String xmppJid;

    private static final long serialVersionUID = -31871889682914674L;

    public Room(int roomId, String name, String topic, DateTime lastActiveDate,
            DateTime created, int ownerUserId, boolean archived, boolean privateRoom,
            String guestAccessURL, String xmppJid) {
        this.roomId = roomId;
        this.name = name;
        this.topic = topic;
        this.lastActiveDate = lastActiveDate;
        this.creationDate = created;
        this.ownerUserId = ownerUserId;
        this.archived = archived;
        this.privateRoom = privateRoom;
        this.guestAccessURL = guestAccessURL;
        this.xmppJid = xmppJid;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public DateTime getLastActiveDate() {
        return lastActiveDate;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public boolean isArchived() {
        return archived;
    }

    public boolean isPrivateRoom() {
        return privateRoom;
    }

    public String getGuestAccessURL() {
        return guestAccessURL;
    }

    public String getXmppJid() {
        return xmppJid;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                          .add("roomId", roomId)
                          .add("name", name)
                          .add("topic", topic)
                          .add("lastActiveDate", lastActiveDate)
                          .add("creationDate", creationDate)
                          .add("ownerUserId", ownerUserId)
                          .add("archived", archived)
                          .add("privateRoom", privateRoom)
                          .add("guestAccessURL", guestAccessURL)
                          .add("xmppJid", xmppJid)
                          .toString();
    }

}
