package com.chatalytics.core.model.data;

import com.google.common.base.MoreObjects;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Represents an entity extracted from a chat message. This is also persisted in the database.
 *
 * @author giannis
 *
 */
@Entity
@Table(name = ChatEntity.ENTITY_TABLE_NAME,
indexes = {@Index(name = "idx_username", columnList = "username"),
           @Index(name = "idx_roomName", columnList = "roomName"),
           @Index(name = "idx_value", columnList = "value")})
@EqualsAndHashCode
@Setter(value = AccessLevel.PROTECTED) // for hibernate
public class ChatEntity implements IMentionable<String> {

    public static final String ENTITY_TABLE_NAME = "ENTITIES";
    public static final String ENTITY_VALUE_COLUMN = "VALUE";
    public static final String OCCURENCES_COLUMN = "OCCURRENCES";
    public static final String MENTION_TIME_COLUMN = "MENTION_TIME";
    public static final String ROOM_NAME_COLUMN = "ROOM_NAME";
    public static final String USER_NAME_COLUMN = "USER_NAME";

    public static final long serialVersionUID = -4845804080646234255L;

    private String username;
    private String roomName;
    private DateTime mentionTime;
    private String value;
    private int occurrences;
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    public ChatEntity(String username, String roomName, DateTime mentionTime,
                      String value, int occurrences) {
        this.username = username;
        this.roomName = roomName;
        this.mentionTime = mentionTime;
        this.value = value;
        this.occurrences = occurrences;
    }

    protected ChatEntity() {} // for jackson

    @Override
    @Column(name = ENTITY_VALUE_COLUMN)
    public String getValue() {
        return value;
    }

    @Override
    @Column(name = OCCURENCES_COLUMN)
    public int getOccurrences() {
        return occurrences;
    }

    @Override
    @Column(name = MENTION_TIME_COLUMN)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    public DateTime getMentionTime() {
        return mentionTime;
    }

    @Override
    @Column(name = USER_NAME_COLUMN)
    public String getUsername() {
        return username;
    }

    @Override
    @Column(name = ROOM_NAME_COLUMN)
    public String getRoomName() {
        return roomName;
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
