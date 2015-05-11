package com.chatalytics.compute.slack.dao;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;

/**
 * Factory for getting an instance of the slack DAO.
 *
 * @author giannis
 *
 */
public class SlackApiDAOFactory {

    private static IChatApiDAO slackDaoImpl;

    private SlackApiDAOFactory() {
        // hide constructor
    }

    public static IChatApiDAO getSlackApiDao(ChatAlyticsConfig config) {
        if (slackDaoImpl == null) {
            slackDaoImpl = new JsonSlackDAO(config);
        }
        return slackDaoImpl;
    }
}
