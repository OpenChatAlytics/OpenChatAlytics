package com.chatalytics.core.model.data;

import org.joda.time.DateTime;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * Summary event for a chat message. This object does not include the actual message and can be
 * safely exposed through the ChatAlytics API.
 *
 * @author giannis
 */
@Entity
@Table(name = MessageSummary.MESSAGE_SUMMARY_TABLE_NAME,
       indexes = {@Index(name = "ms_idx_username", columnList = "username"),
                  @Index(name = "ms_idx_roomName", columnList = "roomName"),
                  @Index(name = "ms_idx_value", columnList = "value"),
                  @Index(name = "ms_idx_bot", columnList = "bot")})
@EqualsAndHashCode
@Setter(value = AccessLevel.PROTECTED) // for hibernate
@ToString
public class MessageSummary implements IMentionable<MessageType> {

    public static final String MESSAGE_SUMMARY_TABLE_NAME = "MESSAGE_SUMMARY";
    public static final String OCCURENCES_COLUMN = "OCCURRENCES";
    public static final String MENTION_TIME_COLUMN = "MENTION_TIME";
    public static final String ROOM_NAME_COLUMN = "ROOM_NAME";
    public static final String USER_NAME_COLUMN = "USER_NAME";
    public static final String TYPE_COLUMN = "VALUE";
    public static final String BOT_COLUMN = "BOT";

    private String username;
    private String roomName;
    private DateTime mentionTime;
    private MessageType value;
    private int occurrences;
    private boolean bot;
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    public MessageSummary(String username, String roomName, DateTime mentionTime,
                          MessageType value, int occurrences, boolean bot) {
        this.username = username;
        this.roomName = roomName;
        this.mentionTime = mentionTime;
        this.value = value;
        this.occurrences = occurrences;
        this.bot = bot;
    }

    protected MessageSummary() {} // for jackson

    @Override
    @Column(name = TYPE_COLUMN)
    public MessageType getValue() {
        return value;
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
    @Column(name = BOT_COLUMN)
    public boolean isBot() {
        return bot;
    }
}
