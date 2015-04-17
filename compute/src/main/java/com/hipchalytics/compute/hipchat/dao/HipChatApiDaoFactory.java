package com.hipchalytics.compute.hipchat.dao;

import com.hipchalytics.compute.config.HipChalyticsConfig;

/**
 * Factory for getting an instance of the hipchat DAO.
 *
 * @author giannis
 *
 */
public class HipChatApiDaoFactory {

    private static IHipChatApiDao hipchatDaoImpl;

    private HipChatApiDaoFactory() {
        // hide constructor
    }

    public static IHipChatApiDao getHipChatApiDao(HipChalyticsConfig hconfig) {
        if (hipchatDaoImpl == null) {
            hipchatDaoImpl = new JsonHipChatDao(hconfig);
        }
        return hipchatDaoImpl;
    }

}
