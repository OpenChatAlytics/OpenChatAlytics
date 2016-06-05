package com.chatalytics.compute.chat.dao.slack;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;

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
            DefaultClientConfig clientConfig = new DefaultClientConfig();
            Client client = Client.create(clientConfig);
            slackDaoImpl = new JsonSlackDAO(config, client);
        }
        return slackDaoImpl;
    }
}
