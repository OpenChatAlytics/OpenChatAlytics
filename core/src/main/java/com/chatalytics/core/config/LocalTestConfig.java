package com.chatalytics.core.config;

/**
 * Config file for testing with a local spout that can emit random messages
 *
 */
public class LocalTestConfig {

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

}
