package com.hipchalytics.compute.db.dao;

import com.hipchalytics.compute.config.HipChalyticsConfig;

/**
 * Factory for constructing {@link IHipChalyticsDao}s.
 *
 * @author giannis
 *
 */
public class HipChalyticsDaoFactory {

    private static IHipChalyticsDao hipchalyticsDaoImpl;

    private HipChalyticsDaoFactory() {
        // hide constructor
    }

    public static IHipChalyticsDao getHipchalyticsDao(HipChalyticsConfig hconfig) {
        if (hipchalyticsDaoImpl == null) {
            hipchalyticsDaoImpl = new HipChalyticsDaoImpl(hconfig);
        }
        return hipchalyticsDaoImpl;
    }
}
