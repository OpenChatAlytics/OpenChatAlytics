package com.chatalytics.core.config;

import java.io.Serializable;
import java.util.List;

public interface ChatConfig extends Serializable {

    /**
     * @return A list of auth tokens to use for authentication
     */
    List<String> getAuthTokens();

    /**
     * @return The base API URL for the chat input source
     */
    String getBaseAPIURL();

    /**
     * @return The character used to signify the beginning an emoji
     */
    char getEmojiStartChar();

    /**
     * @return The character used to signify the end of an emoji
     */
    char getEmojiEndChar();

}
