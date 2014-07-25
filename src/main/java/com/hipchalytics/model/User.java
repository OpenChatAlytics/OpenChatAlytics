package com.hipchalytics.model;

import com.google.common.base.Objects;

import org.joda.time.DateTime;

/**
 * Hipchat user
 *
 * @author giannis
 *
 */
public class User {

    private final int userId;
    private final String email;
    private final boolean deleted;
    private final boolean groupAdmin;
    private final String name;
    private final String mentionName;
    private final String photoUrl;
    private final DateTime lastActiveDate;
    private final DateTime creationDate;
    private final String status;
    private final String statusMessage;
    private final String timezone;
    private final String title;

    public User(int userId, String email, boolean deleted, boolean groupAdmin, String name,
            String mentionName, String photoUrl, DateTime lastActiveDate, DateTime creationDate,
            String status, String statusMessage, String timezone, String title) {
        this.userId = userId;
        this.email = email;
        this.deleted = deleted;
        this.groupAdmin = groupAdmin;
        this.name = name;
        this.mentionName = mentionName;
        this.photoUrl = photoUrl;
        this.lastActiveDate = lastActiveDate;
        this.creationDate = creationDate;
        this.status = status;
        this.statusMessage = statusMessage;
        this.timezone = timezone;
        this.title = title;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean isGroupAdmin() {
        return groupAdmin;
    }

    public String getName() {
        return name;
    }

    public String getMentionName() {
        return mentionName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public DateTime getLastActiveDate() {
        return lastActiveDate;
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this.getClass())
            .add("userId", userId)
            .add("email", email)
            .add("deleted", deleted)
            .add("groupAdmin", groupAdmin)
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
