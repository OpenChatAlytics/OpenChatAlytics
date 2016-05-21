package com.chatalytics.compute.db.dao;

import com.chatalytics.core.model.MessageSummary;
import com.chatalytics.core.model.MessageType;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Service;

import org.joda.time.Interval;

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
     * Gets the total number of message summaries in the given time period with username and room
     * name as optional arguments
     *
     * @param interval
     *            The time interval to search in
     * @param roomName
     *            Optional room name to return count for
     * @param username
     *            Optional username to return count for
     * @return The total count
     */
    int getTotalMessageSummaries(Interval interval, Optional<String> roomName,
                                 Optional<String> username);

    /**
     * Gets the total number of message summaries for a given type in the given time period with
     * username and room name as optional arguments
     *
     * @param type
     *            The type to get counts for
     * @param interval
     *            The time interval to search in
     * @param roomName
     *            Optional room name to return count for
     * @param username
     *            Optional username to return count for
     * @return The total count
     */
    int getTotalMessageSummaries(MessageType type, Interval interval, Optional<String> roomName,
                                 Optional<String> username);
}
