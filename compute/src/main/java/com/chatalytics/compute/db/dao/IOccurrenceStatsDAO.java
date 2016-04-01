package com.chatalytics.compute.db.dao;

import com.chatalytics.core.model.IMentionable;
import com.google.common.base.Optional;

import org.joda.time.Interval;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * A general DAO for doing occurrence statistics on any type T. The type could be any arbitrary
 * entity that implements {@link IMentionable}
 *
 * @author giannis
 *
 * @param <T>
 */
public interface IOccurrenceStatsDAO<T extends IMentionable> extends Closeable {

    /**
     * Persists an value we're interested in doing occurrence statistics on
     *
     * @param value
     *            The value to be persisted
     */
    public void persistValue(T value);


    /**
     * Gets a type <code>T</code> from the database. Note that all fields need to be set.
     *
     * @param entity
     *            Entity to be retrieved
     */
    public T getValue(T value);

    /**
     * Returns all the mention occurrences for type <code>T</code> inside the given
     * <code>interval</code>.
     *
     * @param value
     *            The value to get mentions for
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomName
     *            Optionally supply a room name
     * @param username
     *            Optionally supply a user name
     * @return A list of <code>T</code> representing all the times this entity was mentioned
     *         in the given time period
     */
    public List<T> getAllMentionsForType(String value,
                                         Interval interval,
                                         Optional<String> roomName,
                                         Optional<String> username);

    /**
     * Returns the total number of times a type <code>T</code> was mentioned in the given
     * <code>interval</code>.
     *
     * @param entity
     *            The value of interest
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomName
     *            Optionally supply a room name
     * @param username
     *            Optionally supply a user name
     * @return The total number of times the entity was mentioned in the given time interval
     */
    public long getTotalMentionsForType(String value,
                                        Interval interval,
                                        Optional<String> roomName,
                                        Optional<String> username);

    /**
     * Returns back the top mentioned string representation of a type in the given time
     * <code>interval</code>, and optionally by user name and/or room name
     *
     * @param interval
     *            The time interval to search in
     * @param roomName
     *            Optional room name to filter by
     * @param username
     *            Optional user name to filter by
     * @param resultSize
     *            The number of top entities to return back
     * @return Returns back a map of the string representation of a type to number of occurrences.
     */
    public Map<String, Long> getTopValuesOfType(Interval interval,
                                                Optional<String> roomName,
                                                Optional<String> username,
                                                int resultSize);

    /**
     * Gets the type this DAO is working with
     *
     * @return The type this DAO is performing occurrence stats on
     */
    public Class<T> getType();
}
