package com.chatalytics.compute.db.dao;

import com.google.common.util.concurrent.Service;

import org.joda.time.DateTime;

/**
 * Contains methods for persisting and retrieving objects from the chatalytics store.
 *
 * @author giannis
 *
 */
public interface IChatAlyticsDAO extends Service {

    /**
     * @return The last date and time chat messages were pulled.
     */
    public DateTime getLastMessagePullTime();

    /**
     * Update the last pull time to a new one
     *
     * @param time
     *            The time to update to
     */
    public void setLastMessagePullTime(DateTime time);
}
