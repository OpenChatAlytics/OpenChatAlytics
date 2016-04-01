package com.chatalytics.core.model.json;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public abstract class JsonChatDeserializer<T> extends JsonDeserializer<T> {

    protected String getAsTextOrNull(JsonNode node) {
        if (node == null) {
            return null;
        } else {
            return node.asText();
        }
    }

    protected boolean getAsBooleanOrFalse(JsonNode node) {
        if (node == null) {
            return false;
        } else {
            return node.asBoolean();
        }
    }

}
