package com.hipchalytics.compute.db.dao;

import com.google.common.util.concurrent.AbstractIdleService;
import com.hipchalytics.compute.config.HipChalyticsConfig;
import com.hipchalytics.core.model.HipchatEntity;
import com.hipchalytics.core.model.LastPullTime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

/**
 * Implementation of the {@link IHipChalyticsDao} using SQL lite
 *
 * @author giannis
 *
 */
public class HipChalyticsDaoImpl extends AbstractIdleService implements IHipChalyticsDao {

    private final EntityManager entityManager;
    private final EntityManagerFactory entityManagerFactory;

    public HipChalyticsDaoImpl(HipChalyticsConfig hconfig) {
        this.entityManagerFactory =
            Persistence.createEntityManagerFactory(hconfig.persistenceUnitName);
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
    public void persistEntity(HipchatEntity entity) {
        entityManager.getTransaction().begin();
        String queryStr = String.format("DELETE FROM %s", LastPullTime.class.getSimpleName());
        entityManager.createQuery(queryStr).executeUpdate();
        entityManager.persist(entity);
        entityManager.getTransaction().commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HipchatEntity getEntity(HipchatEntity entity) {
        return entityManager.find(HipchatEntity.class, entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HipchatEntity> getAllEntityMentions(String entity, Interval interval) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<HipchatEntity> query = cb.createQuery(HipchatEntity.class);
        Root<HipchatEntity> from = query.from(HipchatEntity.class);
        ParameterExpression<String> entityParam = cb.parameter(String.class);
        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);
        query.where(cb.equal(from.get("entityValue"), entityParam),
                    cb.between(from.get("mentionTime"), startDateParam,
                               endDateParam));

        return entityManager.createQuery(query)
                            .setParameter(entityParam, entity)
                            .setParameter(startDateParam, interval.getStart())
                            .setParameter(endDateParam, interval.getEnd())
                            .getResultList();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalMentionsForEntity(String entity, Interval interval) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<HipchatEntity> from = query.from(HipchatEntity.class);
        ParameterExpression<String> entityParam = cb.parameter(String.class);
        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);
        Expression<Long> sum = cb.sum(from.get("occurrences"));

        query.select(sum.alias("occurrences"));
        query.where(cb.equal(from.get("entityValue"), entityParam),
                    cb.between(from.get("mentionTime"), startDateParam,
                               endDateParam));

        Long result =  entityManager.createQuery(query)
                                    .setParameter(entityParam, entity)
                                    .setParameter(startDateParam, interval.getStart())
                                    .setParameter(endDateParam, interval.getEnd())
                                    .getSingleResult();

        if (result == null) {
            return 0;
        } else {
            return result;
        }
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
