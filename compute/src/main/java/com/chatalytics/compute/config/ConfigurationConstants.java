package com.chatalytics.compute.config;

import com.chatalytics.core.config.ChatAlyticsConfig;

/**
 * Enum that contains configuration property names found in the storm configuration object.
 *
 * @author giannis
 *
 */
public enum ConfigurationConstants {

    /**
     * Property for the {@link ChatAlyticsConfig} object.
     */
    CHATALYTICS_CONFIG("com.chatalytics.config"),

    /**
     * Property for storing the list of sentiment words
     */
    SENTIMENT_WORDS("com.chatalytics.bolts.sentiment.words");

    public final String txt;

    private ConfigurationConstants(String propertyName) {
        this.txt = propertyName;
    }

}
