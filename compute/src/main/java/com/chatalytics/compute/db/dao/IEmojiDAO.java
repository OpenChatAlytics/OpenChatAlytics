package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.GraphPartition;
import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.compute.matrix.LabeledMTJMatrix;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.model.data.EmojiEntity;
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
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @return A list of {@link EmojiEntity} representing all the times this emoji was mentioned in
     *         the given time period
     */
     List<EmojiEntity> getAllMentionsForEmoji(String emoji,
                                              Interval interval,
                                              List<String> roomNames,
                                              List<String> usernames);

    /**
     * Returns all the mention occurrences of all the emojis inside the given <code>interval</code>
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
     * @return A list of {@link EmojiEntity} representing all the times emojis were mentioned in the
     *         given time period
     */
     List<EmojiEntity> getAllMentions(Interval interval,
                                      List<String> roomNames,
                                      List<String> usernames,
                                      boolean withBots);

    /**
     * Returns the total number of times an emoji was mentioned in the given <code>interval</code>.
     *
     * @param emoji
     *            The emoji of interest
     * @param interval
     *            The interval of interest. Note that the query is inclusive of the start time and
     *            exclusive of the end time.
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @param withBots
     *            Set to true if the result should include mentions by bots
     * @return The total number of times the emoji was mentioned in the given time interval
     */
     int getTotalMentionsForEmoji(String emoji,
                                  Interval interval,
                                  List<String> roomNames,
                                  List<String> usernames,
                                  boolean withBots);

    /**
     * Returns back the top emojis in the given time interval, and optionally by user name and/or
     * room name
     *
     * @param interval
     *            The time interval to search in
     * @param roomNames
     *            Optionally supply a list of room names. This list can be empty
     * @param usernames
     *            Optionally supply a list of user names. This list can be empty
     * @param resultSize
     *            The number of top emojis to return back
     * @param withBots
     *            Set to true if the result should include mentions by bots
     * @return Returns back a map of emoji to number of occurrences.
     */
     Map<String, Long> getTopEmojis(Interval interval,
                                    List<String> roomNames,
                                    List<String> usernames,
                                    int resultSize,
                                    boolean withBots);

    /**
     * Given a time interval this method will return a labeled room by room matrix with all the
     * similar rooms, based on the emoji value clustered together. For more information see
     * {@link GraphPartition#getSimilarityMatrix(LabeledMTJMatrix)}
     *
     * @param interval
     *            The interval to search in
     * @param withBots
     *            Set to true if the result should include mentions by bots
     * @return A labeled matrix
     */
    LabeledDenseMatrix<String> getRoomSimilaritiesByEmoji(Interval interval, boolean withBots);

    /**
     * Given a time interval this method will return a labeled user by user matrix with all the
     * similar users, based on the emoji value clustered together. For more information see
     * {@link GraphPartition#getSimilarityMatrix(LabeledMTJMatrix)}
     *
     * @param interval
     *            The interval to search in
     * @param withBots
     *            Set to true if the result should include mentions by bots
     * @return A labeled matrix
     */
    LabeledDenseMatrix<String> getUserSimilaritiesByEmoji(Interval interval, boolean withBots);

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
