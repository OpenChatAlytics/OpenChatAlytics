package com.chatalytics.compute.db.dao;

import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.EmojiEntity;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.AbstractIdleService;

import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

public class EmojiDAOImpl extends AbstractIdleService implements IEmojiDAO {

    private final EntityManager entityManager;
    private final EntityManagerFactory entityManagerFactory;
    private static final Logger LOG = LoggerFactory.getLogger(EmojiDAOImpl.class);

    public EmojiDAOImpl(ChatAlyticsConfig config) {
        this.entityManagerFactory =
            Persistence.createEntityManagerFactory(config.persistenceUnitName);
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistEmoji(EmojiEntity emoji) {
        entityManager.getTransaction().begin();
        try {
            entityManager.persist(emoji);
            entityManager.getTransaction().commit();
        } catch (PersistenceException e) {
            LOG.error("Cannot store {}. {}", emoji, e.getMessage());
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
        }
    }

    @Override
    public Map<String, Long> getTopEmojis(Interval interval, Optional<String> roomName,
            Optional<String> username, int resultSize) {
        return null;
    }

    @Override
    protected void shutDown() throws Exception {
        entityManager.close();
        entityManagerFactory.close();
    }

    @Override
    protected void startUp() throws Exception {
    }
}
