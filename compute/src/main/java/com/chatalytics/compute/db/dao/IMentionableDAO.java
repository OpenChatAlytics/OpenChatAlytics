package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.GraphPartition;
import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.compute.matrix.LabeledMTJMatrix;
import com.chatalytics.core.model.data.IMentionable;

import org.joda.time.Interval;

import java.io.Closeable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityExistsException;

/**
 * A general DAO for doing occurrence statistics on any type T. The type could be any arbitrary
 * value that implements {@link IMentionable}
 *
 * @author giannis
 *
 * @param <T>
 */
public interface IMentionableDAO<K extends Serializable, T extends IMentionable<K>>
        extends Closeable {

    /**
     * Persists an value we're interested in doing occurrence statistics on
     *
     * @param value
     *            The value to be persisted
     *
     * @throws EntityExistsException if the value already exists
     */
    void persistValue(T value);

    /**
     * Gets a type <code>T</code> from the database. Note that all fields need to be set.
     *
     * @param value
     *            The value to get
     */
    T getValue(T value);

    /**
     * Returns all the mention occurrences for type <code>T</code> inside the given
     * <code>interval</code>.
     *
     * @param value
     *            The value to get mentions for
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @return A list of <code>T</code> representing all the times this value was mentioned
     *         in the given time period
     */
    List<T> getAllMentionsForValue(K value,
                                   Interval interval,
                                   List<String> roomNames,
                                   List<String> usernames);

    /**
     * Returns all the values and all the mention occurrences of type <code>T</code> inside the
     * given <code>interval</code>
     *
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @return A list of <code>T</code> representing all the times this value was mentioned
     *         in the given time period
     */
    List<T> getAllMentions(Interval interval, List<String> roomNames, List<String> usernames);

    /**
     * Returns the total number of times a type <code>T</code> was mentioned in the given
     * <code>interval</code>.
     *
     * @param value
     *            The value of interest
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @return The total number of times the value was mentioned in the given time interval
     */
    int getTotalMentionsForType(K value,
                                Interval interval,
                                List<String> roomNames,
                                List<String> usernames);

    /**
     * Returns the total number of mentions of a particular type this DAO represents
     *
     * @param interval
     *            The interval to search in
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @return The total number of mentions based on the given parameters
     */
    int getTotalMentionsOfType(Interval interval,
                               List<String> roomNames,
                               List<String> usernames);

    /**
     * Returns back the top mentioned values of a type in the given time <code>interval</code>, and
     * optionally by user name and/or room name
     *
     * @param interval
     *            The time interval to search in
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @param resultSize
     *            The number of top entities to return back
     * @return Returns back a map of the value of a type to number of occurrences.
     */
    Map<K, Long> getTopValuesOfType(Interval interval,
                                    List<String> roomName,
                                    List<String> username,
                                    int resultSize);

    /**
     * Returns a sorted map of type to a ratio, where the ratio is the type volume over the total
     * volume. We call this metric ToTV (type over total volume)
     *
     * @param columnName
     *            The column to get the top values for. eg Room or User
     * @param interval
     *            The interval to get the top values in. Note that the start is inclusive and the
     *            end is exclusive
     * @param resultSize
     *            The result size
     * @return A sorted map of top column to ratio
     */
    Map<String, Double> getActiveColumnsByToTV(String columnName, Interval interval, int resultSize);

    /**
     * Returns a sorted map of type to a ratio, where the ratio is the type volume over the total
     * message volume. We call this metric ToMV (type over message volume)
     *
     * @param columnName
     *            The column to get the top values for. eg Room or User
     * @param interval
     *            The interval to get the top values in. Note that the start is inclusive and the
     *            end is exclusive
     * @param resultSize
     *            The result size
     * @return A sorted map of top column to ratio
     */
    Map<String, Double> getActiveColumnsByToMV(String columnName, Interval interval, int resultSize);

    /**
     * Given a time interval this method will return a labeled room by room matrix with all the
     * similar rooms clustered together. For more information see
     * {@link GraphPartition#getSimilarityMatrix(LabeledMTJMatrix)}
     *
     * @param interval
     *            The interval to search in
     * @return A labeled matrix
     */
    LabeledDenseMatrix<String> getRoomSimilaritiesByValue(Interval interval);

    /**
     * Given a time interval this method will return a labeled user by user matrix with all the
     * similar users clustered together. For more information see
     * {@link GraphPartition#getSimilarityMatrix(LabeledMTJMatrix)}
     *
     * @param interval
     *            The interval to search in
     * @return A labeled matrix
     */
    LabeledDenseMatrix<String> getUserSimilaritiesByValue(Interval interval);

    /**
     * Gets the type this DAO is working with
     *
     * @return The type this DAO is performing occurrence stats on
     */
    Class<T> getType();
}
