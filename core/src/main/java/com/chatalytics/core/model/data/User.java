package com.chatalytics.core.model.data;

import org.joda.time.DateTime;

import lombok.Data;

/**
 * Chat user
 *
 * @author giannis
 *
 */
@Data
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

}
