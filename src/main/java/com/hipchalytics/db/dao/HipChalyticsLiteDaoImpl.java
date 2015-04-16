package com.hipchalytics.db.dao;

import com.google.common.util.concurrent.AbstractIdleService;
import com.hipchalytics.config.HipChalyticsConfig;
import com.hipchalytics.model.HipchatEntity;

import org.hibernate.HibernateException;
import org.joda.time.DateTime;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Implementation of the {@link IHipChalyticsDao} using SQL lite
 *
 * @author giannis
 *
 */
public class HipChalyticsLiteDaoImpl extends AbstractIdleService implements IHipChalyticsDao {

    private final HipChalyticsConfig config;
    private final EntityManager entityManager;

    public HipChalyticsLiteDaoImpl(HipChalyticsConfig hconfig) {
        this.config = hconfig;
        this.entityManager = createEntityManager();
    }

    /**
     * Return the current time for now.
     */
    @Override
    public DateTime getLastMessagePullTime() {

        return new DateTime(System.currentTimeMillis());
    }

    @Override
    public void persistEntity(HipchatEntity entity) {
        entityManager.getTransaction().begin();
        entityManager.persist(entity);
        entityManager.getTransaction().commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HipchatEntity getEntity(HipchatEntity entity) {
        return null;
    }

    @Override
    protected void shutDown() throws Exception {
        entityManager.close();
    }

    @Override
    protected void startUp() throws Exception {
    }

    private EntityManager createEntityManager() throws HibernateException {
        EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory("hipchalytics-db");
        return entityManagerFactory.createEntityManager();

    }
}
