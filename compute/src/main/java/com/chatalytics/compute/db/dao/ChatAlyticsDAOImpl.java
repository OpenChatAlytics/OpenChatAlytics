package com.chatalytics.compute.db.dao;

import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.LastPullTime;
import com.google.common.util.concurrent.AbstractIdleService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Implementation of the {@link IChatAlyticsDAO} using SQL lite
 *
 * @author giannis
 *
 */
public class ChatAlyticsDAOImpl extends AbstractIdleService implements IChatAlyticsDAO {

    private final EntityManager entityManager;
    private final EntityManagerFactory entityManagerFactory;

    public ChatAlyticsDAOImpl(ChatAlyticsConfig config) {
        this.entityManagerFactory =
            Persistence.createEntityManagerFactory(config.persistenceUnitName);
        this.entityManager = entityManagerFactory.createEntityManager();
    }

    /**
     * Return the current time for now.
     */
    @Override
    public DateTime getLastMessagePullTime() {
        String query = String.format("FROM %s", LastPullTime.class.getSimpleName());
        List<LastPullTime> result = entityManager.createQuery(query, LastPullTime.class)
                                                 .getResultList();

        if (result == null || result.size() == 0) {
            return new DateTime(0).withZone(DateTimeZone.UTC);
        }
        return result.get(0).getTime();
    }

    /**
     * Update the last pull time
     *
     * @param time
     *            Time to set it to
     */
    @Override
    public void setLastMessagePullTime(DateTime time) {
        LastPullTime lastPullTime = entityManager.find(LastPullTime.class, LastPullTime.ID);

        entityManager.getTransaction().begin();
        if (lastPullTime != null) {
            lastPullTime.setTime(time);
        } else {
            lastPullTime = new LastPullTime(time);
            entityManager.persist(lastPullTime);
        }
        entityManager.getTransaction().commit();
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
