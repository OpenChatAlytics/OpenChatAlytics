package com.chatalytics.core.emoji;

import com.chatalytics.core.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.io.Resources;

import java.io.IOException;
import java.util.Map;

/**
 * Includes utility methods for getting emojis
 *
 * @author giannis
 */
public class LocalEmojiUtils {

    /**
     * A map of emojis to unicode
     *
     * @return A map of emoji names to unicode
     * @throws RuntimeException
     *             When the emoji file can't be found, read or parsed
     */
    public static Map<String, String> getUnicodeEmojis(ObjectMapper objectMapper) {
        MapType mapType = TypeFactory.defaultInstance()
                                     .constructMapType(Map.class, String.class, String.class);
        try {
            return objectMapper.readValue(Resources.getResource(Constants.EMOJI_RESOURCE), mapType);
        } catch (IOException e) {
            throw new RuntimeException("Can't read emojis", e);
        }
    }
}
