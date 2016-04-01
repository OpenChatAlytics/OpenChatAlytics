package com.chatalytics.compute.db.dao;

import com.chatalytics.core.model.EmojiEntity;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.Service;

import org.joda.time.Interval;

import java.util.Map;

public interface IEmojiDAO extends Service {

    /**
     * Persists an emoji to the database
     *
     * @param emoji
     *            The emoji to be persisted
     */
    public void persistEmoji(EmojiEntity emoji);

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
     *            The number of top entities to return back
     * @return Returns back a map of emoji to number of occurrences.
     */
    public Map<String, Long> getTopEmojis(Interval interval,
                                            Optional<String> roomName,
                                            Optional<String> username,
                                            int resultSize);

}
