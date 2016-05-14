package com.chatalytics.core.model;

import com.google.common.base.MoreObjects;

import org.joda.time.DateTime;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Chat user
 *
 * @author giannis
 *
 */
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class User {

    private final String userId;
    private final String email;
    private final boolean deleted;
    private final boolean groupAdmin;
    private final boolean bot;
    private final String name;
    private final String mentionName;
    private final String photoUrl;
    private final DateTime lastActiveDate;
    private final DateTime creationDate;
    private final String status;
    private final String statusMessage;
    private final String timezone;
    private final String title;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                          .add("userId", userId)
                          .add("email", email)
                          .add("deleted", deleted)
                          .add("groupAdmin", groupAdmin)
                          .add("bot", bot)
                          .add("name", name)
                          .add("mentionName", mentionName)
                          .add("photoUrl", photoUrl)
                          .add("lastActiveDate", lastActiveDate)
                          .add("creationDate", creationDate)
                          .add("status", status)
                          .add("statusMessage", statusMessage)
                          .add("timezone", timezone)
                          .add("title", title)
                          .toString();
    }

}
