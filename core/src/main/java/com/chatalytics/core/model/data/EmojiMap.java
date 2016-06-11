package com.chatalytics.core.model.data;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

/**
 * An object representing a map of emojis
 *
 * @author giannis
 */
@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class EmojiMap {
    /**
     * Emoji name to emoji URL
     */
    private final Map<String, String> customEmojis;
    /**
     * Emoji name to unicode value
     */
    private final Map<String, String> unicodeEmojis;
}
