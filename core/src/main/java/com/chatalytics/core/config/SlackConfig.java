package com.chatalytics.core.config;

import java.util.List;

public class SlackConfig implements ChatConfig {

    private static final long serialVersionUID = 2857449595685095484L;

    public List<String> authTokens;

    public String baseAPIURL = "https://slack.com/api/";

    public char emojiStartChar = ':';

    public char emojiEndChar = ':';

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
