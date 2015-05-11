package com.chatalytics.core.model.hipchat.json;

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
 * Deserializes {@link Room}s
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

        int roomId = node.get("room_id").asInt();
        String name = node.get("name").asText();
        String topic = node.get("topic").asText();

        DateTime lastActiveDate = new DateTime(node.get("last_active").asLong() * 1000L);
        DateTime creationDate = new DateTime(node.get("created").asLong() * 1000L);
        int ownerUserId = node.get("owner_user_id").asInt();
        boolean archived = node.get("is_archived").asBoolean();
        boolean privateRoom = node.get("is_private").asBoolean();
        String guestAccessURL = null;
        JsonNode guestAccessJonEl = node.get("guest_access_url");
        if (guestAccessJonEl != null) {
            guestAccessURL = guestAccessJonEl.asText();
        }
        String xmppJid = node.get("xmpp_jid").asText();
        return new Room(roomId, name, topic, lastActiveDate, creationDate,
                        ownerUserId, archived, privateRoom, guestAccessURL, xmppJid);
    }

}
