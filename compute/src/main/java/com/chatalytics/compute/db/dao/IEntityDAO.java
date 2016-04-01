package com.chatalytics.compute.db.dao;

import com.chatalytics.core.model.ChatEntity;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Service;

import org.joda.time.Interval;

import java.util.List;
import java.util.Map;

public interface IEntityDAO extends Service {

    /**
     * Persists an entity to the database
     *
     * @param entity
     *            The entity to be persisted
     */
    public void persistEntity(ChatEntity entity);

    /**
     * Gets an entity from the database. Note that all fields in entity need to be set. Use
     * {@link #getEntityMentions(String, DateTime, DateTime)} if you want to get the total number of
     * mentions for an entity in a given time range.
     *
     * @param entity
     *            Entity to be retrieved
     */
    public ChatEntity getEntity(ChatEntity entity);

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
     * @return A list of {@link ChatEntity} representing all the times this entity was mentioned
     *         in the given time period
     */
    public List<ChatEntity> getAllMentionsForEntity(String entity,
                                                    Interval interval,
                                                    Optional<String> roomName,
                                                    Optional<String> username);

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
    public long getTotalMentionsForEntity(String entity,
                                          Interval interval,
                                          Optional<String> roomName,
                                          Optional<String> username);


    /**
     * Returns back the top mentioned entities in the given time interval, and optionally by user
     * name and/or room name
     *
     * @param interval
     *            The time interval to search in
     * @param roomName
     *            Optional room name to filter by
     * @param username
     *            Optional user name to filter by
     * @param resultSize
     *            The number of top entities to return back
     * @return Returns back a map of entity value to number of occurrences.
     */
    public Map<String, Long> getTopEntities(Interval interval,
                                            Optional<String> roomName,
                                            Optional<String> username,
                                            int resultSize);
}
