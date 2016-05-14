package com.chatalytics.core.model;

import com.google.common.base.MoreObjects;

import org.joda.time.DateTime;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * Bean that represents a chat message with <code>int</code>s for the user and room.
 *
 * @author giannis
 *
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class Message implements Serializable {

    private final DateTime date;
    private final String fromName;
    private final String fromUserId;
    private final String message;
    private final String roomId;
    private final MessageType type;

    private static final long serialVersionUID = -4370348419961560257L;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                          .add("date", date)
                          .add("fromName", fromName)
                          .add("fromUserId", fromUserId)
                          .add("message", message)
                          .add("roomId", roomId)
                          .add("type", type)
                          .toString();
    }

}
