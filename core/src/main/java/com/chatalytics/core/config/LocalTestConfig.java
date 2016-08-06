package com.chatalytics.core.config;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Config file for testing with a local spout that can emit random messages
 *
 */
public class LocalTestConfig implements ChatConfig {

    private static final long serialVersionUID = -5527223071338609488L;

    /**
     * The amount of time to pass by before another message gets emitted
     */
    public long sleepMs = 5 * 1000; // 2 seconds

    /**
     * Set the seed to a value if you want a predictable random user and room generation
     */
    public Long randomSeed;

    /**
     * Number of fake users to create
     */
    public int numUsers = 10;

    /**
     * Number of fake rooms to create
     */
    public int numRooms = 3;

    /**
     * The filename to read senteces. Emitted messages will be based on these sentences
     */
    public String messageCorpusFile;

    public char emojiStartChar = ':';

    public char emojiEndChar = ':';

    @Override
    public List<String> getAuthTokens() {
        return ImmutableList.of();
    }

    @Override
    public String getBaseAPIURL() {
        return "http://dummy";
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
        return true;
    }

    @Override
    public boolean includeArchivedRooms() {
        return true;
    }

}
