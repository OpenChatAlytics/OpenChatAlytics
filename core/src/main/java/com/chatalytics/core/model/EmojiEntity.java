package com.chatalytics.core.model;

import com.google.common.base.MoreObjects;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = EmojiEntity.EMOJI_TABLE_NAME)
@EqualsAndHashCode
@Setter(value = AccessLevel.PROTECTED) // for hibernate
public class EmojiEntity implements IMentionable {

    private static final long serialVersionUID = 7180644692083145759L;

    public static final String EMOJI_TABLE_NAME = "EMOJI";
    public static final String EMOJI_COLUMN = "EMOJI_VALUE";
    public static final String OCCURENCES_COLUMN = "OCCURRENCES";
    public static final String MENTION_TIME_COLUMN = "MENTION_TIME";
    public static final String ROOM_NAME_COLUMN = "ROOM_NAME";
    public static final String USER_NAME_COLUMN = "USER_NAME";

    /**
     * Emoji alias without ':'
     */
    private String emoji;
    private String username;
    private String roomName;
    private DateTime mentionTime;
    private int occurrences;

    public EmojiEntity(String emoji, String username, String roomName, DateTime mentionTime,
                       int occurrences) {
        this.emoji = emoji;
        this.username = username;
        this.roomName = roomName;
        this.mentionTime = mentionTime;
        this.occurrences = occurrences;
    }

    @Id
    @Column(name = EMOJI_COLUMN)
    public String getEmoji() {
        return emoji;
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
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
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
                          .add("emoji", emoji)
                          .add("occurrences", occurrences)
                          .add("mentionTime", mentionTime)
                          .add("username", username)
                          .add("roomName", roomName)
                          .toString();
    }
}
