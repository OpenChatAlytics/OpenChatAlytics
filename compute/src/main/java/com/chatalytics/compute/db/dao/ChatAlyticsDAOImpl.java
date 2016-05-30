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

    private final EntityManager entityManager;

    public ChatAlyticsDAOImpl(EntityManagerFactory entityManagerFactory) {
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
    }

    @Override
    protected void startUp() throws Exception {
    }
}
