package com.chatalytics.compute.db.dao;

import com.chatalytics.core.model.data.LastPullTime;
import com.google.common.util.concurrent.AbstractIdleService;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Implementation of the {@link IChatAlyticsDAO} using SQL lite
 *
 * @author giannis
 *
 */
public class ChatAlyticsDAOImpl extends AbstractIdleService implements IChatAlyticsDAO {

    private final EntityManagerFactory entityManagerFactory;

    public ChatAlyticsDAOImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Return the current time for now.
     */
    @Override
    public DateTime getLastMessagePullTime() {
        String query = String.format("FROM %s", LastPullTime.class.getSimpleName());

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try {
            List<LastPullTime> result = entityManager.createQuery(query, LastPullTime.class)
                                                     .getResultList();

            if (result == null || result.size() == 0) {
                return new DateTime(0).withZone(DateTimeZone.UTC);
            }
            return result.get(0).getTime();
        } finally {
            entityManager.close();
        }
    }

    /**
     * Update the last pull time
     *
     * @param time
     *            Time to set it to
     */
    @Override
    public void setLastMessagePullTime(DateTime time) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        LastPullTime lastPullTime = entityManager.find(LastPullTime.class, LastPullTime.ID);
        try {
            entityManager.getTransaction().begin();
            if (lastPullTime != null) {
                lastPullTime.setTime(time);
            } else {
                lastPullTime = new LastPullTime(time);
                entityManager.persist(lastPullTime);
            }
            entityManager.getTransaction().commit();
        } finally {
            entityManager.close();
        }
    }

    @Override
    protected void shutDown() throws Exception { }

    @Override
    protected void startUp() throws Exception { }
}
