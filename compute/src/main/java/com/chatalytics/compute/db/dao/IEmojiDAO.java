package com.chatalytics.compute.db.dao;

import com.chatalytics.core.model.data.EmojiEntity;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Service;

import org.joda.time.Interval;

import java.util.List;
import java.util.Map;

public interface IEmojiDAO extends Service {

    /**
     * Persists an emoji to the database
     *
     * @param emoji
     *            The emoji to be persisted
     */
     void persistEmoji(EmojiEntity emoji);

    /**
     * Gets an emoji from the database. Note that all fields in {@link EmojiEntity} need to be set.
     * Use {@link #getEmojiMentions(String, DateTime, DateTime)} if you want to get the total number
     * of mentions for an emoji in a given time range.
     *
     * @param emoji
     *            Emoji to be retrieved
     */
    EmojiEntity getEmoji(EmojiEntity emoji);

    /**
     * Returns all the mention occurrences for an emoji inside the given <code>interval</code>.
     *
     * @param emoji
     *            The emoji to get mentions for
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomName
     *            Optionally supply a room name
     * @param username
     *            Optionally supply a user name
     * @return A list of {@link EmojiEntity} representing all the times this emoji was mentioned in
     *         the given time period
     */
     List<EmojiEntity> getAllMentionsForEmoji(String emoji,
                                              Interval interval,
                                              Optional<String> roomName,
                                              Optional<String> username);

    /**
     * Returns all the mention occurrences of all the emojis inside the given <code>interval</code>
     *
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomName Optionally supply a room name
     * @param username Optionally supply a user name
     * @return A list of {@link EmojiEntity} representing all the times emojis were mentioned in the
     *         given time period
     */
     List<EmojiEntity> getAllMentions(Interval interval,
                                      Optional<String> roomName,
                                      Optional<String> username);

    /**
     * Returns the total number of times an emoji was mentioned in the given <code>interval</code>.
     *
     * @param emoji
     *            The emoji of interest
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomName
     *            Optionally supply a room name
     * @param username
     *            Optionally supply a user name
     * @return The total number of times the emoji was mentioned in the given time interval
     */
     int getTotalMentionsForEmoji(String emoji,
                                  Interval interval,
                                  Optional<String> roomName,
                                  Optional<String> username);

    /**
     * Returns back the top emojis in the given time interval, and optionally by user name and/or
     * room name
     *
     * @param interval
     *            The time interval to search in
     * @param roomName
     *            Optional room name to filter by
     * @param username
     *            Optional user name to filter by
     * @param resultSize
     *            The number of top emojis to return back
     * @return Returns back a map of emoji to number of occurrences.
     */
     Map<String, Long> getTopEmojis(Interval interval,
                                    Optional<String> roomName,
                                    Optional<String> username,
                                    int resultSize);

     /**
      * Returns a sorted map of user to a ratio, where the ratio is the emoji volume over the total
      * volume of all emojis in a time range. We call this metric EoTV (emoji over total volume)
      *
      * @param interval
      *            The interval to get the top values in. Note that the start is inclusive and the
      *            end is exclusive
      * @param resultSize
      *            The result size
      * @return A sorted map of top users to ratio
      */
     Map<String, Double> getTopUsersByEoTV(Interval interval, int resultSize);

     /**
      * Returns a sorted map of rooms to a ratio, where the ratio is the emoji volume over the
      * total emoji volume. We call this metric EoTV (emoji over total volume)
      *
      * @param interval
      *            The interval to get the top values in. Note that the start is inclusive and the
      *            end is exclusive
      * @param resultSize
      *            The result size
      * @return A sorted map of top room to ratio
      */
     Map<String, Double> getTopRoomsByEoTV(Interval interval, int resultSize);

}
