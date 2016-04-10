package com.chatalytics.core.json;

import com.chatalytics.core.InputSourceType;
import com.chatalytics.core.model.hipchat.json.HipChatJsonModule;
import com.chatalytics.core.model.slack.json.SlackJsonModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

/**
 * Builds a JSON {@link ObjectMapper} for deserializing and serializing objects.
 *
 * @author giannis
 *
 */
public class JsonObjectMapperFactory {

    private JsonObjectMapperFactory() {}

    public static ObjectMapper createObjectMapper(InputSourceType inputSource) {
        ObjectMapper objectMapper = new ObjectMapper();

        if (inputSource == InputSourceType.SLACK || inputSource == InputSourceType.SLACK_BACKFILL) {
            objectMapper.registerModule(new SlackJsonModule());
        } else if (inputSource == InputSourceType.HIPCHAT) {
            objectMapper.registerModule(new HipChatJsonModule());
        }

        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

        return objectMapper;
    }

}
