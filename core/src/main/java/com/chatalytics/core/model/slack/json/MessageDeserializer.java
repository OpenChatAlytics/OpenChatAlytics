package com.chatalytics.core.model.slack.json;

import com.chatalytics.core.model.Message;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * Deserializes slack {@link Message}s
 *
 * @author giannis
 *
 */
public class MessageDeserializer extends JsonDeserializer<Message> {

    @Override
    public Message deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {

        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);

        JsonNode fromNameNode = node.get("username");
        String fromName = null;
        if (fromNameNode != null) {
            fromName = fromNameNode.asText();
        }

        JsonNode fromUserIdNode = node.get("user");
        String fromUserId;
        if (fromUserIdNode == null) {
            fromUserId = fromName;
        } else {
            fromUserId = fromUserIdNode.asText();
        }

        String message = node.get("text").asText();

        String timestampStr = node.get("ts").asText();
        String[] timestampElementsArr = timestampStr.split("\\.");
        long seconds = Long.parseLong(timestampElementsArr[0]);
        long nanos = Long.parseLong(timestampElementsArr[1]);
        long timeInMillis = seconds * 1000 + nanos / 1000;
        DateTime date = new DateTime(timeInMillis);

        return new Message(date, fromName, fromUserId, message);
    }

}
