package com.chatalytics.compute.db.dao;

import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.EmojiEntity;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.AbstractIdleService;

import org.joda.time.Interval;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Implementation of the {@link IEmojiDAO} that can store and retrieve emojis
 *
 * @author giannis
 *
 */
public class EmojiDAOImpl extends AbstractIdleService implements IEmojiDAO {

    private final IMentionableDAO<EmojiEntity> occurrenceStatsDAO;
    private final EntityManagerFactory entityManagerFactory;

    public EmojiDAOImpl(ChatAlyticsConfig config) {
        this.entityManagerFactory =
            Persistence.createEntityManagerFactory(config.persistenceUnitName);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        this.occurrenceStatsDAO = new MentionableDAO<>(entityManager, EmojiEntity.class, "emoji");
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
                                                    Optional<String> roomName,
                                                    Optional<String> username) {
        return occurrenceStatsDAO.getAllMentionsForValue(emoji, interval, roomName, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EmojiEntity> getAllMentions(Interval interval, Optional<String> roomName,
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
    protected void startUp() throws Exception {
    }

    /**
     * Closes the entity manager and the entity manager factory
     */
    @Override
    protected void shutDown() throws Exception {
        occurrenceStatsDAO.close();
        entityManagerFactory.close();
    }
}
