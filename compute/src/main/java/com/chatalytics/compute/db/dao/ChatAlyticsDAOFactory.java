package com.chatalytics.compute.db.dao;

import com.chatalytics.core.config.ChatAlyticsConfig;

/**
 * Factory for constructing {@link ChatAlyticsDAO}s.
 *
 * @author giannis
 *
 */
public class ChatAlyticsDAOFactory {

    private static ChatAlyticsDAO chatalyticsDaoImpl;

    private ChatAlyticsDAOFactory() {
        // hide constructor
    }

    public static ChatAlyticsDAO getChatAlyticsDao(ChatAlyticsConfig hconfig) {
        if (chatalyticsDaoImpl == null) {
            chatalyticsDaoImpl = new ChatAlyticsDAOImpl(hconfig);
        }
        return chatalyticsDaoImpl;
    }
}
