package com.hipchalytics.db.dao;

import com.hipchalytics.config.HipChalyticsConfig;
import com.hipchalytics.model.Entity;

import org.joda.time.DateTime;

/**
 * Implementation of the {@link IHipChalyticsDao} using SQL lite
 *
 * @author giannis
 *
 */
public class HipChalyticsLiteDaoImpl implements IHipChalyticsDao {

    private final HipChalyticsConfig config;

    public HipChalyticsLiteDaoImpl(HipChalyticsConfig hconfig) {
        this.config = hconfig;
    }

    /**
     * Return the current time for now.
     */
    @Override
    public DateTime getLastMessagePullTime() {
        return new DateTime(System.currentTimeMillis());
    }

    @Override
    public void persistEntity(Entity entity) {
        // TODO Auto-generated method stub

    }

    @Override
    public void getEntity(Entity entity) {
        // TODO Auto-generated method stub

    }

}
