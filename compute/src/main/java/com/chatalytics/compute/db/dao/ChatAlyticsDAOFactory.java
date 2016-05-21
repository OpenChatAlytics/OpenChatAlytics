package com.chatalytics.compute.db.dao;

import com.chatalytics.core.config.ChatAlyticsConfig;
import com.google.common.annotations.VisibleForTesting;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Factory for constructing DAOs.
 *
 * @author giannis
 *
 */
public class ChatAlyticsDAOFactory {

    /**
     * There should only be one instance of the factory across the application
     */
    private static EntityManagerFactory entityManagerFactory;

    private ChatAlyticsDAOFactory() {
        // hide constructor
    }

    public static IChatAlyticsDAO createChatAlyticsDao(ChatAlyticsConfig config) {
        EntityManagerFactory emf = getEntityManagerFactory(config);
        return new ChatAlyticsDAOImpl(emf);
    }

    public static IEntityDAO createEntityDAO(ChatAlyticsConfig config) {
        EntityManagerFactory emf = getEntityManagerFactory(config);
        return new EntityDAOImpl(emf);
    }

    public static IEmojiDAO createEmojiDAO(ChatAlyticsConfig config) {
        EntityManagerFactory emf = getEntityManagerFactory(config);
        return new EmojiDAOImpl(emf);
    }

    public static IMessageSummaryDAO createMessageSummaryDAO(ChatAlyticsConfig config) {
        EntityManagerFactory emf = getEntityManagerFactory(config);
        return new MessageSummaryDAOImpl(emf);
    }

    /**
     * Closes the entity manager factory. This will invalidate all open {@link EntityManager}s
     */
    public static void closeEntityManagerFactory() {
        if (entityManagerFactory != null) {
            entityManagerFactory.close();
        }
    }

    @VisibleForTesting
    protected static EntityManagerFactory getEntityManagerFactory(ChatAlyticsConfig config) {
        if (entityManagerFactory == null) {
            String persistenceName = config.persistenceUnitName;
            entityManagerFactory = Persistence.createEntityManagerFactory(persistenceName);
        }
        return entityManagerFactory;
    }
}
