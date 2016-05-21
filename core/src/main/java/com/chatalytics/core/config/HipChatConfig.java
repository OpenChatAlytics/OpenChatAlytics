package com.chatalytics.core.config;

import java.util.List;

public class HipChatConfig implements ChatConfig {

    private static final long serialVersionUID = -6648260488672146737L;

    public List<String> authTokens;

    public String baseAPIURL = "https://api.hipchat.com/v1/";

    public char emojiStartChar = '(';

    public char emojiEndChar = ')';

    @Override
    public List<String> getAuthTokens() {
        return authTokens;
    }

    @Override
    public String getBaseAPIURL() {
        return baseAPIURL;
    }

    @Override
    public char getEmojiStartChar() {
        return emojiStartChar;
    }

    @Override
    public char getEmojiEndChar() {
        return emojiEndChar;
    }

}
