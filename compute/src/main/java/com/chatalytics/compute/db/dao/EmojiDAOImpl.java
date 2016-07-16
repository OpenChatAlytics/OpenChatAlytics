package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.model.data.EmojiEntity;
import com.google.common.util.concurrent.AbstractIdleService;

import org.joda.time.Interval;

import java.util.List;
import java.util.Map;

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
        occurrenceStatsDAO.persistValue(emoji);
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
                                                    List<String> roomNames,
                                                    List<String> usernames) {
        return occurrenceStatsDAO.getAllMentionsForValue(emoji, interval, roomNames, usernames);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EmojiEntity> getAllMentions(Interval interval,
                                            List<String> roomNames,
                                            List<String> usernames,
                                            boolean withBots) {
        return occurrenceStatsDAO.getAllMentions(interval, roomNames, usernames, withBots);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalMentionsForEmoji(String emoji,
                                        Interval interval,
                                        List<String> roomNames,
                                        List<String> usernames,
                                        boolean withBots) {
        return occurrenceStatsDAO.getTotalMentionsForType(emoji, interval, roomNames, usernames,
                                                          withBots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getTopEmojis(Interval interval,
                                          List<String> roomNames,
                                          List<String> usernames,
                                          int resultSize,
                                          boolean withBots) {
        return occurrenceStatsDAO.getTopValuesOfType(interval, roomNames, usernames, resultSize,
                                                     withBots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LabeledDenseMatrix<String> getRoomSimilaritiesByEmoji(Interval interval,
                                                                 boolean withBots) {
        return occurrenceStatsDAO.getRoomSimilaritiesByValue(interval, withBots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LabeledDenseMatrix<String> getUserSimilaritiesByEmoji(Interval interval,
                                                                 boolean withBots) {
        return occurrenceStatsDAO.getUserSimilaritiesByValue(interval, withBots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Double> getActiveUsersByMethod(Interval interval,
                                                      ActiveMethod method,
                                                      int resultSize,
                                                      boolean withBots) {
        if (method == ActiveMethod.ToTV) {
            return occurrenceStatsDAO.getActiveColumnsByToTV("username", interval, resultSize,
                                                             withBots);
        } else if (method == ActiveMethod.ToMV) {
            return occurrenceStatsDAO.getActiveColumnsByToMV("username", interval, resultSize,
                                                             withBots);
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
                                                      int resultSize,
                                                      boolean withBots) {
        if (method == ActiveMethod.ToTV) {
            return occurrenceStatsDAO.getActiveColumnsByToTV("roomName", interval, resultSize,
                                                             withBots);
        } else if (method == ActiveMethod.ToMV) {
            return occurrenceStatsDAO.getActiveColumnsByToMV("roomName", interval, resultSize,
                                                             withBots);
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
