package com.chatalytics.core.model;

import org.joda.time.DateTime;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Summary event for a chat message. This object does not include the actual message and can be
 * safely exposed through the ChatAlytics API.
 *
 * @author giannis
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class MessageSummary implements IMentionable<Integer> {

    private static final long serialVersionUID = 4610523559744723974L;

    private final String username;
    private final String roomName;
    private final DateTime mentionTime;

    /**
     * Occurrences will always be 1. This field is here to make JSON serialization easier
     */
    private final int occurrences;
    private final Integer value;

    public MessageSummary(String username, String roomName, DateTime mentionTime) {
        this.username = username;
        this.roomName = roomName;
        this.mentionTime = mentionTime;
        this.occurrences = 1;
        this.value = occurrences;
    }
}
