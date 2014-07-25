package com.hipchalytics.db.dao;

import com.hipchalytics.config.HipChalyticsConfig;

import org.joda.time.DateTime;

/**
 * Implementation of the {@link IHipChalyticsDao}.
 *
 * @author giannis
 *
 */
public class HipChalyticsDaoImpl implements IHipChalyticsDao {

    private final HipChalyticsConfig config;

    public HipChalyticsDaoImpl(HipChalyticsConfig hconfig) {
        this.config = hconfig;
    }

    /**
     * Return the current time for now.
     */
    @Override
    public DateTime getLastMessagePullTime() {
        return new DateTime(System.currentTimeMillis());
    }

}
