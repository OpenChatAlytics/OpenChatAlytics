package com.hipchalytics.model;

import com.google.common.base.Objects;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Bean that represents a hipchat message with <code>int</code>s for the user and room.
 *
 * @author giannis
 *
 */
public class Message implements Serializable {

    private final DateTime date;
    private final String fromName;
    private final int fromUserId;
    private final String message;
    private int roomId;

    private static final long serialVersionUID = -4370348419961560257L;

    public Message(DateTime date, String fromName, int fromUserId, int roomId, String message) {
        this.date = date;
        this.fromName = fromName;
        this.fromUserId = fromUserId;
        this.roomId = roomId;
        this.message = message;
    }

    public DateTime getDate() {
        return date;
    }

    public String getFromName() {
        return fromName;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this.getClass())
            .add("date", date)
            .add("fromName", fromName)
            .add("fromUserId", fromUserId)
            .add("message", message)
            .toString();
    }

}
