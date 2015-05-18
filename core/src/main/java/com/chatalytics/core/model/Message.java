package com.chatalytics.core.model;

import com.google.common.base.MoreObjects;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Bean that represents a chat message with <code>int</code>s for the user and room.
 *
 * @author giannis
 *
 */
public class Message implements Serializable {

    private final DateTime date;
    private final String fromName;
    private final String fromUserId;
    private final String message;

    private static final long serialVersionUID = -4370348419961560257L;

    public Message(DateTime date, String fromName, String fromUserId, String message) {
        this.date = date;
        this.fromName = fromName;
        this.fromUserId = fromUserId;
        this.message = message;
    }

    public DateTime getDate() {
        return date;
    }

    public String getFromName() {
        return fromName;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                          .add("date", date)
                          .add("fromName", fromName)
                          .add("fromUserId", fromUserId)
                          .add("message", message)
                          .toString();
    }

}
