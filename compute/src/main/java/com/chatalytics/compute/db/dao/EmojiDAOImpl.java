package com.chatalytics.compute.db.dao;

import com.chatalytics.core.config.ChatAlyticsConfig;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.AbstractIdleService;

import org.joda.time.Interval;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EmojiDAOImpl extends AbstractIdleService implements IEmojiDAO {

    private final EntityManager entityManager;
    private final EntityManagerFactory entityManagerFactory;

    public EmojiDAOImpl(ChatAlyticsConfig config) {
        this.entityManagerFactory =
            Persistence.createEntityManagerFactory(config.persistenceUnitName);
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    @Override
    public Map<String, Long> getTopEntities(Interval interval, Optional<String> roomName,
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
