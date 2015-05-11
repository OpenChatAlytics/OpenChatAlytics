package com.chatalytics.core.model.slack.json;

import com.chatalytics.core.model.Room;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * Deserializes slack {@link Room}s
 *
 * @author giannis
 *
 */
public class RoomDeserializer extends JsonDeserializer<Room> {

    @Override
    public Room deserialize(JsonParser jp, DeserializationContext context) throws IOException,
            JsonProcessingException {

        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);

        String roomId = node.get("id").asText();
        String name = node.get("name").asText();
        String topic = node.get("topic").get("value").asText();

        DateTime creationDate = new DateTime(node.get("created").asLong() * 1000L);
        String ownerUserId = node.get("purpose").get("creator").asText();
        boolean archived = node.get("is_archived").asBoolean();
        boolean privateRoom = false;
        return new Room(roomId, name, topic, null, creationDate, ownerUserId, archived,
                        privateRoom, null, null);
    }

}
