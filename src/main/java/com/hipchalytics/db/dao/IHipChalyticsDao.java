package com.hipchalytics.db.dao;

import com.hipchalytics.model.Entity;

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

    /**
     * Persists an entity to the database
     *
     * @param entity
     *            The entity to be persisted
     */
    public void persistEntity(Entity entity);

    /**
     * Gets an entity from the database
     *
     * @param entity
     *            Entity to be retrieved
     */
    public void getEntity(Entity entity);

}
