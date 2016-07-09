package com.chatalytics.compute.db.dao;

import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.model.data.ChatEntity;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.model.data.MessageType;
import com.google.common.util.concurrent.Service;

import org.joda.time.Interval;

import java.util.List;
import java.util.Map;

public interface IMessageSummaryDAO extends Service {

    /**
     * Persists a message summary
     *
     * @param messageSummary
     *            The message summary to persist
     */
    void persistMessageSummary(MessageSummary messageSummary);

    /**
     * Retrieves a message summary
     *
     * @param messageSummary
     *            The message summary to be retrieved
     * @return A message summary
     */
    MessageSummary getMessageSummary(MessageSummary messageSummary);

    /**
     * Returns all the mention occurrences for a {@link MessageType} inside the given
     * <code>interval</code>.
     *
     * @param type
     *            The {@link MessageType} to return {@link MessageSummary}s for
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
     List<MessageSummary> getAllMessageSummariesForType(MessageType type,
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
      * @return A list of {@link ChatEntity} representing all the times this entity was mentioned
      *         in the given time period
      */
      List<MessageSummary> getAllMessageSummaries(Interval interval,
                                                  List<String> roomNames,
                                                  List<String> usernames);

    /**
     * Gets the total number of message summaries in the given time period with username and room
     * name as optional arguments
     *
     * @param interval
     *            The time interval to search in
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @return The total count
     */
    int getTotalMessageSummaries(Interval interval,
                                 List<String> roomNames,
                                 List<String> usernames);

    /**
     * Gets the total number of message summaries for a given type in the given time period with
     * username and room name as optional arguments
     *
     * @param type
     *            The type to get counts for
     * @param interval
     *            The time interval to search in
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @return The total count
     */
    int getTotalMessageSummariesForType(MessageType type,
                                        Interval interval,
                                        List<String> roomNames,
                                        List<String> usernames);

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
     * @return A sorted map of top users to ratio
     */
    Map<String, Double> getActiveUsersByMethod(Interval interval,
                                               ActiveMethod method,
                                               int resultSize);

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
     * @return A sorted map of top room to ratio
     */
    Map<String, Double> getActiveRoomsByMethod(Interval interval,
                                               ActiveMethod method,
                                               int resultSize);
}
