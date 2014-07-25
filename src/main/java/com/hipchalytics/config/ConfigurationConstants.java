package com.hipchalytics.config;

/**
 * Enum that contains configuration property names found in the storm configuration object.
 *
 * @author giannis
 *
 */
public enum ConfigurationConstants {

    /**
     * Property for the {@link HipChalyticsConfig} object.
     */
    HIPCHALYTICS_CONFIG("com.hipchalytics.config"),

    /**
     * The property for getting the base URL for the hipchat API.
     */
    HIPCHAT_API_URL("com.hipchalytics.hipchat.api.url"),

    /**
     * Contains the list of authentication tokens for the hipchat API.
     */
    HIPCHAT_AUTH_TOKENS("com.hipchalytics.hipchat.auth.tokens");

    public final String txt;

    private ConfigurationConstants(String propertyName) {
        this.txt = propertyName;
    }

}
