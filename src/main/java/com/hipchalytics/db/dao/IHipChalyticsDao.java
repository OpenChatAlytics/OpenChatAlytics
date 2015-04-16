package com.hipchalytics.db.dao;

import com.google.common.util.concurrent.Service;
import com.hipchalytics.model.HipchatEntity;

import org.joda.time.DateTime;

/**
 * Contains methods for persisting and retrieving objects from the hipchalytics store.
 *
 * @author giannis
 *
 */
public interface IHipChalyticsDao extends Service {

    /**
     * @return The last date and time hipchat messages were pulled.
     */
    public DateTime getLastMessagePullTime();

    /**
     * Persists an entity to the database
     *
     * @param entity
     *            The entity to be persisted
     */
    public void persistEntity(HipchatEntity entity);

    /**
     * Gets an entity from the database
     *
     * @param entity
     *            Entity to be retrieved
     */
    public HipchatEntity getEntity(HipchatEntity entity);

}
