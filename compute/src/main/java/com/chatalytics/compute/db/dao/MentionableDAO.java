package com.chatalytics.compute.db.dao;

import com.chatalytics.core.model.IMentionable;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

/**
 * Implementation of {@link IMentionableDAO} that can store and retrieve {@link IMentionable}
 * types. {@link MentionableDAO#close()} should be called on end
 *
 * @author giannis
 *
 * @param <T>
 *            The storing type. Should implement {@link IMentionable}
 */
public class MentionableDAO<T extends IMentionable> implements IMentionableDAO<T> {

    private static final Logger LOG = LoggerFactory.getLogger(MentionableDAO.class);

    private final EntityManager entityManager;
    private final Class<T> type;
    private final String typeColumnName;

    protected MentionableDAO(EntityManager entityManager,
                                 Class<T> type,
                                 String typeColumnName) {
        this.entityManager = entityManager;
        this.type = type;
        this.typeColumnName = typeColumnName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistValue(T value) {
        entityManager.getTransaction().begin();
        try {
            entityManager.persist(value);
            entityManager.getTransaction().commit();
        } catch (PersistenceException e) {
            LOG.error("Cannot store {}. {}", value, e.getMessage());
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getValue(T value) {
        return entityManager.find(type, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> getAllMentionsForType(String value, Interval interval, Optional<String> roomName,
                                         Optional<String> username) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(type);
        Root<T> from = query.from(type);
        ParameterExpression<String> valueParam = cb.parameter(String.class);
        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);

        List<Predicate> wherePredicates = Lists.newArrayListWithCapacity(4);
        wherePredicates.add(cb.equal(from.get(typeColumnName), valueParam));
        wherePredicates.add(cb.between(from.get("mentionTime"), startDateParam, endDateParam));

        // Add the optional parameters
        ParameterExpression<String> roomNameParam = null;
        if (roomName.isPresent()) {
            roomNameParam = cb.parameter(String.class);
            wherePredicates.add(cb.equal(from.get("roomName"), roomNameParam));
        }
        ParameterExpression<String> usernameParam = null;
        if (username.isPresent()) {
            usernameParam = cb.parameter(String.class);
            wherePredicates.add(cb.equal(from.get("username"), usernameParam));
        }

        query.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

        TypedQuery<T> finalQuery = entityManager.createQuery(query)
                                                .setParameter(valueParam, value)
                                                .setParameter(startDateParam, interval.getStart())
                                                .setParameter(endDateParam, interval.getEnd());
        if (roomName.isPresent()) {
            finalQuery.setParameter(roomNameParam, roomName.get());
        }
        if (username.isPresent()) {
            finalQuery.setParameter(usernameParam, username.get());
        }
        return finalQuery.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getTotalMentionsForType(String value, Interval interval, Optional<String> roomName,
                                        Optional<String> username) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<T> from = query.from(type);
        ParameterExpression<String> valueParam = cb.parameter(String.class);
        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);
        Expression<Long> sum = cb.sum(from.get("occurrences"));

        query.select(sum.alias("occurrences"));

        List<Predicate> wherePredicates = Lists.newArrayListWithCapacity(4);
        wherePredicates.add(cb.equal(from.get(typeColumnName), valueParam));
        wherePredicates.add(cb.between(from.get("mentionTime"), startDateParam, endDateParam));

        // Add the optional parameters
        ParameterExpression<String> roomNameParam = null;
        if (roomName.isPresent()) {
            roomNameParam = cb.parameter(String.class);
            wherePredicates.add(cb.equal(from.get("roomName"), roomNameParam));
        }
        ParameterExpression<String> usernameParam = null;
        if (username.isPresent()) {
            usernameParam = cb.parameter(String.class);
            wherePredicates.add(cb.equal(from.get("username"), usernameParam));
        }

        query.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

        TypedQuery<Long> finalQuery = entityManager.createQuery(query)
                                                   .setParameter(valueParam, value)
                                                   .setParameter(startDateParam,
                                                                 interval.getStart())
                                                   .setParameter(endDateParam, interval.getEnd());
        if (roomName.isPresent()) {
            finalQuery.setParameter(roomNameParam, roomName.get());
        }
        if (username.isPresent()) {
            finalQuery.setParameter(usernameParam, username.get());
        }

        List<Long> result = finalQuery.getResultList();

        if (result == null || result.isEmpty() || result.get(0) == null) {
            return 0;
        } else {
            return result.get(0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getTopValuesOfType(Interval interval, Optional<String> roomName,
                                                Optional<String> username, int resultSize) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<T> from = query.from(type);
        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);

        Path<String> typeValuePath = from.get(typeColumnName);
        Selection<String> typeValueAlias = typeValuePath.alias("typeValue_sel");

        Expression<Long> occurrencesSum = cb.sum(from.get("occurrences"));
        Selection<Long> occurrencesSumAlias = occurrencesSum.alias("occurrences_sum");

        // do where clause
        List<Predicate> wherePredicates = Lists.newArrayListWithCapacity(3);
        wherePredicates.add(cb.between(from.get("mentionTime"), startDateParam, endDateParam));

        // Add the optional parameters
        ParameterExpression<String> roomNameParam = null;
        if (roomName.isPresent()) {
            roomNameParam = cb.parameter(String.class);
            wherePredicates.add(cb.equal(from.get("roomName"), roomNameParam));
        }
        ParameterExpression<String> usernameParam = null;
        if (username.isPresent()) {
            usernameParam = cb.parameter(String.class);
            wherePredicates.add(cb.equal(from.get("username"), usernameParam));
        }
        query.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

        query.multiselect(typeValueAlias, occurrencesSumAlias);
        query.groupBy(typeValuePath);
        query.orderBy(cb.desc(occurrencesSum));
        TypedQuery<Tuple> finalQuery = entityManager.createQuery(query)
                                                    .setMaxResults(resultSize)
                                                    .setParameter(startDateParam,
                                                                  interval.getStart())
                                                    .setParameter(endDateParam, interval.getEnd());
        if (roomName.isPresent()) {
            finalQuery.setParameter(roomNameParam, roomName.get());
        }
        if (username.isPresent()) {
            finalQuery.setParameter(usernameParam, username.get());
        }
        List<Tuple> resultList = finalQuery.getResultList();
        // linked hashmap to preserve order
        Map<String, Long> result = Maps.newLinkedHashMap();
        for (Tuple tuple : resultList) {
            result.put(tuple.get(typeValueAlias), tuple.get(occurrencesSumAlias));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getType() {
        return type;
    }


    /**
     * Closes the entity manager
     */
    @Override
    public void close() {
        entityManager.close();
    }

}
