package com.chatalytics.core.model.slack.json;

import com.chatalytics.core.model.User;
import com.chatalytics.core.model.json.JsonChatDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Deserializes slack {@link User}s
 *
 * @author giannis
 *
 */
public class UserDeserializer extends JsonChatDeserializer<User> {

    @Override
    public User deserialize(JsonParser jp, DeserializationContext context)
            throws IOException, JsonProcessingException {

        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);

        String userId = node.get("id").asText();
        String mentionName = node.get("name").asText();
        boolean deleted = node.get("deleted").asBoolean();
        boolean groupAdmin = getAsBooleanOrFalse(node.get("is_admin"));
        String statusMessage = getAsTextOrNull(node.get("status"));
        String timezone = getAsTextOrNull(node.get("tz"));

        JsonNode profileNode = node.get("profile");
        String name = profileNode.get("real_name").asText();
        String title = getAsTextOrNull(profileNode.get("title"));
        String email = getAsTextOrNull(profileNode.get("email"));
        String photoUrl = profileNode.get("image_32").asText();

        return new User(userId, email, deleted, groupAdmin, name, mentionName, photoUrl, null,
                        null, null, statusMessage, timezone, title);
    }

}
