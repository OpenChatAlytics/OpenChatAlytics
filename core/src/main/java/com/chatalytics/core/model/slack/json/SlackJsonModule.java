package com.chatalytics.core.model.slack.json;

import com.chatalytics.core.model.Room;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * JSON serialization module for Slack objects. It adds deserializers for the domain objects found
 * in the model package.
 *
 * @author giannis
 *
 */
public class SlackJsonModule extends SimpleModule {

    public SlackJsonModule() {
        super("SlackJsonModule", new Version(1, 0, 0, "SNAP"));
        addDeserializer(Room.class, new RoomDeserializer());
    }

}
