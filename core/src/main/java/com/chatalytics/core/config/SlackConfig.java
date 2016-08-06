package com.chatalytics.core.config;

import java.util.List;

public class SlackConfig implements ChatConfig {

    private static final long serialVersionUID = 2857449595685095484L;

    public List<String> authTokens;

    public String baseAPIURL = "https://slack.com/api/";

    public char emojiStartChar = ':';

    public char emojiEndChar = ':';

    /**
     * Optional start date. The spout will start processing records on and after this date. That
     * means that it's inclusive of the date
     */
    public String startDate;

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
