package com.chatalytics.compute.hipchat.dao;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;

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
            hipchatDaoImpl = new JsonHipChatDAO(config);
        }
        return hipchatDaoImpl;
    }

}
