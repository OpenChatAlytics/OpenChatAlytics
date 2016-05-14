package com.chatalytics.core.model.hipchat.json;

import com.chatalytics.core.model.User;
import com.chatalytics.core.model.json.JsonChatDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import org.joda.time.DateTime;

import java.io.IOException;

/**
 * Deserializes hipchat {@link User}s
 *
 * @author giannis
 *
 */
public class UserDeserializer extends JsonChatDeserializer<User> {

    @Override
    public User deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {

        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);
        int userId = node.get("user_id").asInt();
        String email = node.get("email").asText();
        boolean deleted = node.get("is_deleted").asBoolean();
        boolean groupAdmin = node.get("is_group_admin").asBoolean();
        String name = node.get("name").asText();
        String mentionName = node.get("mention_name").asText();
        String photoUrl = node.get("photo_url").asText();
        DateTime lastActiveDate = new DateTime(node.get("last_active").asLong() * 1000L);
        DateTime creationDate = new DateTime(node.get("created").asLong() * 1000L);
        String status = node.get("status").asText();
        String statusMessage = node.get("status_message").asText();
        String timezone = node.get("timezone").asText();
        String title = node.get("title").asText();

        return new User(String.valueOf(userId), email, deleted, groupAdmin, false, name,
                        mentionName, photoUrl, lastActiveDate, creationDate, status, statusMessage,
                        timezone, title);
    }

}
