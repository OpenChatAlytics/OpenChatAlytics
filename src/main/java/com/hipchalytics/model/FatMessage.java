package com.hipchalytics.model;

import java.io.Serializable;

/**
 * Bean that represents a hipchat message. This bean contains actual {@link Room} and {@link User}
 * objects instead of IDs. The slimmer version of this object is {@link Message}.
 *
 * @author giannis
 *
 */
public class FatMessage implements Serializable {

    private final Message message;
    private final User user;
    private final Room room;

    private static final long serialVersionUID = 6236626325756335748L;

    public FatMessage(Message message, User user, Room room) {
        this.message = message;
        this.user = user;
        this.room = room;
    }

    public Message getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public Room getRoom() {
        return room;
    }

}
