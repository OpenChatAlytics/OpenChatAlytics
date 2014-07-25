package com.hipchalytics.db.dao;

import org.joda.time.DateTime;

/**
 * Contains methods for persisting and retrieving objects from the hipchalytics store.
 *
 * @author giannis
 *
 */
public interface IHipChalyticsDao {

    /**
     * @return The last date and time hipchat messages were pulled.
     */
    public DateTime getLastMessagePullTime();

}
