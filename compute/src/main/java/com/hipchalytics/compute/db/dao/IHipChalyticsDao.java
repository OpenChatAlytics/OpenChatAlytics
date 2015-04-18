package com.hipchalytics.compute.db.dao;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.Service;
import com.hipchalytics.core.model.HipchatEntity;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.List;

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
     * Update the last pull time to a new one
     *
     * @param time
     *            The time to update to
     */
    public void setLastMessagePullTime(DateTime time);

    /**
     * Persists an entity to the database
     *
     * @param entity
     *            The entity to be persisted
     */
    public void persistEntity(HipchatEntity entity);

    /**
     * Gets an entity from the database. Note that all fields in entity need to be set. Use
     * {@link #getEntityMentions(String, DateTime, DateTime)} if you want to get the total number of
     * mentions for an entity in a given time range.
     *
     * @param entity
     *            Entity to be retrieved
     */
    public HipchatEntity getEntity(HipchatEntity entity);

    /**
     * Returns all the mention occurrences for an entity inside the given <code>interval</code>.
     *
     * @param entity
     *            The entity to get mentions for
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomName
     *            Optionally supply a room name
     * @param username
     *            Optionally supply a user name
     * @return A list of {@link HipchatEntity} representing all the times this entity was mentioned
     *         in the given time period
     */
    public List<HipchatEntity> getAllEntityMentions(String entity, Interval interval,
            Optional<String> roomName, Optional<String> username);

    /**
     * Returns the total number of times an entity was mentioned in the given <code>interval</code>.
     *
     * @param entity
     *            The entity of interest
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomName
     *            Optionally supply a room name
     * @param username
     *            Optionally supply a user name
     * @return The total number of times the entity was mentioned in the given time interval
     */
    public long getTotalMentionsForEntity(String entity, Interval interval,
            Optional<String> roomName, Optional<String> username);
}
