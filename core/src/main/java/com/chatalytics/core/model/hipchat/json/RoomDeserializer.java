package com.chatalytics.core.model.hipchat.json;

import com.chatalytics.core.model.data.Room;
import com.chatalytics.core.model.json.JsonChatDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import org.joda.time.DateTime;

import java.io.IOException;

/**
 * Deserializes hipchat {@link Room}s
 *
 * @author giannis
 *
 */
public class RoomDeserializer extends JsonChatDeserializer<Room> {

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
        String guestAccessURL = getAsTextOrNull(node.get("guest_access_url"));
        String xmppJid = node.get("xmpp_jid").asText();
        return new Room(String.valueOf(roomId), name, topic, lastActiveDate, creationDate,
                        String.valueOf(ownerUserId), archived, privateRoom, guestAccessURL,
                        xmppJid);
    }

}
