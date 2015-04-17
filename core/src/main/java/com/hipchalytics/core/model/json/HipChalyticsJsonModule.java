package com.hipchalytics.core.model.json;

import com.hipchalytics.core.model.Message;
import com.hipchalytics.core.model.Room;
import com.hipchalytics.core.model.User;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * JSON serialization module. It adds deserializers for the domain objects found in the model
 * package.
 *
 * @author giannis
 *
 */
public class HipChalyticsJsonModule extends SimpleModule {

    public HipChalyticsJsonModule() {
        super("HipChalyticsJsonModule", new Version(1, 0, 0, "SNAP"));
        addDeserializer(Room.class, new RoomDeserializer());
        addDeserializer(User.class, new UserDeserializer());
        addDeserializer(Message.class, new MessageDeserializer());
    }

}
