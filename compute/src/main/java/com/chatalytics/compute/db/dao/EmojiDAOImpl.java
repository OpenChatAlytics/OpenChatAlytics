package com.chatalytics.compute.db.dao;

import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.model.data.EmojiEntity;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.AbstractIdleService;

import org.joda.time.Interval;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManagerFactory;

/**
 * Implementation of the {@link IEmojiDAO} that can store and retrieve emojis
 *
 * @author giannis
 *
 */
public class EmojiDAOImpl extends AbstractIdleService implements IEmojiDAO {

    private final IMentionableDAO<String, EmojiEntity> occurrenceStatsDAO;

    public EmojiDAOImpl(EntityManagerFactory entityManagerFactory) {
        this.occurrenceStatsDAO = new MentionableDAO<>(entityManagerFactory, EmojiEntity.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistEmoji(EmojiEntity emoji) {
        try {
            occurrenceStatsDAO.persistValue(emoji);
        } catch (EntityExistsException e) {
            EmojiEntity existingValue = occurrenceStatsDAO.getValue(emoji);
            int newOccurrences = emoji.getOccurrences() + existingValue.getOccurrences();
            EmojiEntity mergedEmoji = new EmojiEntity(emoji.getValue(), newOccurrences,
                                                      emoji.getMentionTime(), emoji.getUsername(),
                                                      emoji.getRoomName());
            occurrenceStatsDAO.mergeValue(mergedEmoji);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EmojiEntity getEmoji(EmojiEntity emoji) {
        return occurrenceStatsDAO.getValue(emoji);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EmojiEntity> getAllMentionsForEmoji(String emoji,
                                                    Interval interval,
                                                    Optional<String> roomName,
                                                    Optional<String> username) {
        return occurrenceStatsDAO.getAllMentionsForValue(emoji, interval, roomName, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EmojiEntity> getAllMentions(Interval interval,
                                            Optional<String> roomName,
                                            Optional<String> username) {
        return occurrenceStatsDAO.getAllMentions(interval, roomName, username);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalMentionsForEmoji(String emoji,
                                        Interval interval,
                                        Optional<String> roomName,
                                        Optional<String> username) {
        return occurrenceStatsDAO.getTotalMentionsForType(emoji, interval, roomName, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getTopEmojis(Interval interval,
                                          Optional<String> roomName,
                                          Optional<String> username,
                                          int resultSize) {
        return occurrenceStatsDAO.getTopValuesOfType(interval, roomName, username, resultSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Double> getActiveUsersByMethod(Interval interval,
                                                      ActiveMethod method,
                                                      int resultSize) {
        if (method == ActiveMethod.ToTV) {
            return occurrenceStatsDAO.getActiveColumnsByToTV("username", interval, resultSize);
        } else if (method == ActiveMethod.ToMV) {
            return occurrenceStatsDAO.getActiveColumnsByToMV("username", interval, resultSize);
        } else {
            throw new UnsupportedOperationException(String.format("Method %s not supported",
                                                                  method));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Double> getActiveRoomsByMethod(Interval interval,
                                                      ActiveMethod method,
                                                      int resultSize) {
        if (method == ActiveMethod.ToTV) {
            return occurrenceStatsDAO.getActiveColumnsByToTV("roomName", interval, resultSize);
        } else if (method == ActiveMethod.ToMV) {
            return occurrenceStatsDAO.getActiveColumnsByToMV("roomName", interval, resultSize);
        } else {
            throw new UnsupportedOperationException(String.format("Method %s not supported",
                                                                  method));
        }
    }

    /**
     * Does nothing
     */
    @Override
    protected void startUp() throws Exception {
    }

    /**
     * Closes the underlying {@link MentionableDAO}.
     */
    @Override
    protected void shutDown() throws Exception {
        occurrenceStatsDAO.close();
    }
}
