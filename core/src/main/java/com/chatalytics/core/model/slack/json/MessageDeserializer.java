package com.chatalytics.core.model.slack.json;

import com.chatalytics.core.model.data.Message;
import com.chatalytics.core.model.data.MessageType;
import com.chatalytics.core.model.json.JsonChatDeserializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;

import org.joda.time.DateTime;

import java.io.IOException;

/**
 * Deserializes slack {@link Message}s
 *
 * @author giannis
 *
 */
public class MessageDeserializer extends JsonChatDeserializer<Message> {

    @Override
    public Message deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {

        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);

        String channelId = getAsTextOrNull(node.get("channel"));
        if (channelId != null && channelId.equals("C0G1JEKRU")) {
            System.out.println("here");
        }
        String timestampStr = node.get("ts").asText();
        String[] timestampElementsArr = timestampStr.split("\\.");
        long seconds = Long.parseLong(timestampElementsArr[0]);
        long nanos = Long.parseLong(timestampElementsArr[1]);
        long timeInMillis = seconds * 1000 + nanos / 1000;
        DateTime date = new DateTime(timeInMillis);
        String fromName = getAsTextOrNull(node.get("username"));

        MessageType messageType = getMessageType(node);
        JsonNode fromUserIdNode = null;

        if (messageType == MessageType.MESSAGE_CHANGED) {
            node = node.get("message");
        } else if (messageType == MessageType.BOT_MESSAGE) {
            fromUserIdNode = node.get("bot_id");
        }

        JsonNode attachmentNode = node.get("attachments");
        boolean isShare = false;
        if (attachmentNode != null) {
            // just get the first one
            attachmentNode = attachmentNode.iterator().next();
            JsonNode isShareNode = attachmentNode.get("is_share");
            if (isShareNode != null) {
                isShare = isShareNode.asBoolean();
            }
        }

        if (messageType != MessageType.BOT_MESSAGE) {
            fromUserIdNode = node.get("user");
        }

        if (messageType == MessageType.MESSAGE && isShare) {
            messageType = MessageType.MESSAGE_SHARE;
        }

        String fromUserId;
        if (fromUserIdNode == null) {
            fromUserId = fromName;
        } else {
            fromUserId = fromUserIdNode.asText();
        }

        String message = getAsTextOrNull(node.get("text"));

        if ((message == null || message.isEmpty()) && attachmentNode != null) {
            message = getAsTextOrNull(attachmentNode.get("pretext"));
        }
        if ((message == null || message.isEmpty()) && attachmentNode != null) {
            message = getAsTextOrNull(attachmentNode.get("fallback"));
        }

        return new Message(date, fromName, fromUserId, message, channelId, messageType);
    }

    /**
     * Tries to identify the {@link MessageType} by starting from subtype and moving to type. If it
     * can't be identified it returns {@link MessageType#UNKNOWN}.
     *
     * @param node
     *            The node which may contain type and/or subtype
     * @return A {@link MessageType}
     */
    private MessageType getMessageType(JsonNode node) {

        String type = getAsTextOrNull(node.get("type"));
        String subtype = getAsTextOrNull(node.get("subtype"));

        if (subtype != null) {
            try {
                return MessageType.fromType(subtype);
            } catch (IllegalArgumentException e) {
                    return MessageType.fromTypeOrUnknown(subtype);
            }
        } else if (type != null) {
            return MessageType.fromTypeOrUnknown(type);
        } else {
            return MessageType.UNKNOWN;
        }
    }

}
