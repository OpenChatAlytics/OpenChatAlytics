package com.chatalytics.core.config;

import java.util.List;

public class SlackConfig implements ChatConfig {

    private static final long serialVersionUID = 2857449595685095484L;

    public List<String> authTokens;

    public String baseAPIURL = "https://slack.com/api/";

    public char emojiStartChar = ':';

    public char emojiEndChar = ':';

    public boolean includePrivateRooms = false;

    public boolean includeArchivedRooms = false;

    /**
     * Specifies the initial sleep timeout for connecting to the realtime slack API
     */
    public int sourceConnectionSleepIntervalMs = 1000;
    /**
     * Specifies the maximum sleep time when retrying
     */
    public int sourceConnectionBackoffMaxSleepMs = 10 * 1000 * 60; // 10 mins
    /**
     * Specifies the global max time for the initial connection to the slack API before giving up
     * and propagating an exception up.
     */
    public int sourceConnectionMaxMs = 48 * 60 * 1000;

    /**
     * Optional start date. The spout will start processing records on and after this date. That
     * means that it's inclusive of the date. The format is ISO 8601.
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

    @Override
    public boolean includePrivateRooms() {
        return includePrivateRooms;
    }

    @Override
    public boolean includeArchivedRooms() {
        return includeArchivedRooms;
    }

}
