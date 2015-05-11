package com.chatalytics.core.model.hipchat.json;

import com.chatalytics.core.model.Message;
import com.chatalytics.core.model.Room;
import com.chatalytics.core.model.User;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * JSON serialization module for HipChat. It adds deserializers for the domain objects found in the
 * model package.
 *
 * @author giannis
 *
 */
public class HipChatJsonModule extends SimpleModule {

    public HipChatJsonModule() {
        super("HipChatJsonModule", new Version(1, 0, 0, "SNAP"));
        addDeserializer(Room.class, new RoomDeserializer());
        addDeserializer(User.class, new UserDeserializer());
        addDeserializer(Message.class, new MessageDeserializer());
    }

}
