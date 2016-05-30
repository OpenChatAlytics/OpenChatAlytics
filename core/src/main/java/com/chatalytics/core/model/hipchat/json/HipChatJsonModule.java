package com.chatalytics.core.model.hipchat.json;

import com.chatalytics.core.model.data.Message;
import com.chatalytics.core.model.data.Room;
import com.chatalytics.core.model.data.User;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;


/**
 * JSON serialization module for HipChat. It adds deserializers for the domain objects found in the
 * model package.
 *
 * @author giannis
 *
 */
public class HipChatJsonModule extends SimpleModule {

    private static final long serialVersionUID = -2674682182161301239L;

    public HipChatJsonModule() {
        super("HipChatJsonModule", new Version(1, 0, 0, "SNAP", "group", "artifact"));
        addDeserializer(Room.class, new RoomDeserializer());
        addDeserializer(User.class, new UserDeserializer());
        addDeserializer(Message.class, new MessageDeserializer());
    }

}
