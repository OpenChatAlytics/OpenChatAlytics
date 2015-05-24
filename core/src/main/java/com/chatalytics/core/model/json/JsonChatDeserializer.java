package com.chatalytics.core.model.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonDeserializer;

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
