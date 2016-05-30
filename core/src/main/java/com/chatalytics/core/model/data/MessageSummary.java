package com.chatalytics.core.model.data;

import com.google.common.base.MoreObjects;

import org.joda.time.DateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Summary event for a chat message. This object does not include the actual message and can be
 * safely exposed through the ChatAlytics API.
 *
 * @author giannis
 */
@Entity
@Table(name = MessageSummary.MESSAGE_SUMMARY_TABLE_NAME)
@EqualsAndHashCode
@AllArgsConstructor
@Setter(value = AccessLevel.PROTECTED) // for hibernate
public class MessageSummary implements IMentionable<MessageType> {

    private static final long serialVersionUID = 4610523559744723974L;
    public static final String MESSAGE_SUMMARY_TABLE_NAME = "MESSAGE_SUMMARY";
    public static final String OCCURENCES_COLUMN = "OCCURRENCES";
    public static final String MENTION_TIME_COLUMN = "MENTION_TIME";
    public static final String ROOM_NAME_COLUMN = "ROOM_NAME";
    public static final String USER_NAME_COLUMN = "USER_NAME";
    public static final String TYPE_COLUMN = "VALUE";

    private String username;
    private String roomName;
    private DateTime mentionTime;
    private MessageType value;
    private int occurrences;

    protected MessageSummary() {} // for jackson

    @Override
    @Id
    @Column(name = TYPE_COLUMN)
    public MessageType getValue() {
        return value;
    }

    @Override
    @Id
    @Column(name = USER_NAME_COLUMN)
    public String getUsername() {
        return username;
    }

    @Override
    @Id
    @Column(name = ROOM_NAME_COLUMN)
    public String getRoomName() {
        return roomName;
    }

    @Override
    @Id
    @Column(name = MENTION_TIME_COLUMN)
    public DateTime getMentionTime() {
        return mentionTime;
    }

    @Override
    @Column(name = OCCURENCES_COLUMN)
    public int getOccurrences() {
        return occurrences;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                          .add("value", value)
                          .add("occurrences", occurrences)
                          .add("mentionTime", mentionTime)
                          .add("username", username)
                          .add("roomName", roomName)
                          .toString();
    }
}
