package com.chatalytics.compute.chat.dao.hipchat;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Factory for getting an instance of the hipchat DAO.
 *
 * @author giannis
 *
 */
public class HipChatApiDAOFactory {

    private static IChatApiDAO hipchatDaoImpl;

    private HipChatApiDAOFactory() {
        // hide constructor
    }

    public static IChatApiDAO getHipChatApiDao(ChatAlyticsConfig config) {
        if (hipchatDaoImpl == null) {
            DefaultClientConfig clientConfig = new DefaultClientConfig();
            Client client = Client.create(clientConfig);
            hipchatDaoImpl = new JsonHipChatDAO(config, client);
        }
        return hipchatDaoImpl;
    }

}
