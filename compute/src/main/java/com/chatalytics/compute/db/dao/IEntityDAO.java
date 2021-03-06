package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.GraphPartition;
import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.compute.matrix.LabeledMTJMatrix;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.model.data.ChatEntity;
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
    void persistEntity(ChatEntity entity);

    /**
     * Gets an entity from the database. Note that all fields in entity need to be set. Use
     * {@link #getEntityMentions(String, DateTime, DateTime)} if you want to get the total number of
     * mentions for an entity in a given time range.
     *
     * @param entity
     *            Entity to be retrieved
     */
    ChatEntity getEntity(ChatEntity entity);

    /**
     * Returns all the mention occurrences for an entity inside the given <code>interval</code>.
     *
     * @param entity
     *            The entity to get mentions for
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @return A list of {@link ChatEntity} representing all the times this entity was mentioned in
     *         the given time period
     */
     List<ChatEntity> getAllMentionsForEntity(String entity,
                                              Interval interval,
                                              List<String> roomNames,
                                              List<String> usernames);

    /**
     * Returns all the mention occurrences for an entity inside the given <code>interval</code>.
     *
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @param withBots
     *            Set to true if the result should include mentions by bots
     * @return A list of {@link ChatEntity} representing all the times this entity was mentioned in
     *         the given time period
     */
    List<ChatEntity> getAllMentions(Interval interval,
                                    List<String> roomNames,
                                    List<String> usernames,
                                    boolean withBots);

    /**
     * Returns the total number of times an entity was mentioned in the given <code>interval</code>.
     *
     * @param entity
     *            The entity of interest
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @param withBots
     *            Set to true if the result should include mentions by bots
     * @return The total number of times the entity was mentioned in the given time interval
     */
     int getTotalMentionsForEntity(String entity,
                                   Interval interval,
                                   List<String> roomNames,
                                   List<String> usernames,
                                   boolean withBots);


    /**
     * Returns back the top mentioned entities in the given time interval, and optionally by user
     * name and/or room name
     *
     * @param interval
     *            The time interval to search in
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @param resultSize
     *            The number of top entities to return back
     * @param withBots
     *            Set to true if the result should include mentions by bots
     * @return Returns back a map of entity value to number of occurrences.
     */
    Map<String, Long> getTopEntities(Interval interval,
                                     List<String> roomNames,
                                     List<String> usernames,
                                     int resultSize,
                                     boolean withBots);

    /**
     * Given a time interval this method will return a labeled room by room matrix with all the
     * similar rooms, based on the entity value clustered together. For more information see
     * {@link GraphPartition#getSimilarityMatrix(LabeledMTJMatrix)}
     *
     * @param interval
     *            The interval to search in
     * @param withBots
     *            Set to true if the result should include mentions by bots
     * @return A labeled matrix
     */
    LabeledDenseMatrix<String> getRoomSimilaritiesByEntity(Interval interval, boolean withBots);

    /**
     * Given a time interval this method will return a labeled user by user matrix with all the
     * similar users, based on the entity value clustered together. For more information see
     * {@link GraphPartition#getSimilarityMatrix(LabeledMTJMatrix)}
     *
     * @param interval
     *            The interval to search in
     * @param withBots
     *            Set to true if the result should include mentions by bots
     * @return A labeled matrix
     */
    LabeledDenseMatrix<String> getUserSimilaritiesByEntity(Interval interval, boolean withBots);

    /**
     * Returns a sorted map of users to a ratio, where the ratio is one of {@link ActiveMethod}s
     *
     * @param interval
     *            The interval to get the top values in. Note that the start is inclusive and the
     *            end is exclusive
     * @param method
     *            The method to compute top users for
     * @param resultSize
     *            The result size
     * @param withBots
     *            Set to true if the result should include mentions by bots
     * @return A sorted map of top users to ratio
     */
    Map<String, Double> getActiveUsersByMethod(Interval interval,
                                               ActiveMethod method,
                                               int resultSize,
                                               boolean withBots);

    /**
     * Returns a sorted map of rooms to a ratio, where the ratio is one of {@link ActiveMethod}s
     *
     * @param interval
     *            The interval to get the top values in. Note that the start is inclusive and the
     *            end is exclusive
     * @param method
     *            The method to compute top users for
     * @param resultSize
     *            The result size
     * @param withBots
     *            Set to true if the result should include mentions by bots
     * @return A sorted map of top room to ratio
     */
    Map<String, Double> getActiveRoomsByMethod(Interval interval,
                                               ActiveMethod method,
                                               int resultSize,
                                               boolean withBots);
}
