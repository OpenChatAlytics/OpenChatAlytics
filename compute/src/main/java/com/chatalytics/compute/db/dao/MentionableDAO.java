package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.GraphPartition;
import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.compute.matrix.LabeledMatrix;
import com.chatalytics.core.model.IMentionable;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
public class MentionableDAO<K extends Serializable, T extends IMentionable<K>>
        implements IMentionableDAO<K, T> {

    private static final Logger LOG = LoggerFactory.getLogger(MentionableDAO.class);
    private static final String TYPE_COLUMN_NAME = "value";

    private final EntityManager entityManager;
    private final Class<T> type;

    protected MentionableDAO(EntityManagerFactory entityManagerFactory, Class<T> type) {
        this.entityManager = entityManagerFactory.createEntityManager();
        this.type = type;
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
    public List<T> getAllMentionsForValue(K value, Interval interval,
                                          Optional<String> roomName, Optional<String> username) {
        return internalGetAllMentionsForValue(Optional.of(value), interval, roomName, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> getAllMentions(Interval interval, Optional<String> roomName,
                                  Optional<String> username) {
        return internalGetAllMentionsForValue(Optional.absent(), interval, roomName, username);
    }

    public List<T> internalGetAllMentionsForValue(Optional<K> value,
                                                  Interval interval,
                                                  Optional<String> roomName,
                                                  Optional<String> username) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(type);
        Root<T> from = query.from(type);
        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);

        List<Predicate> wherePredicates = Lists.newArrayListWithCapacity(4);
        wherePredicates.add(cb.between(from.get("mentionTime"), startDateParam, endDateParam));

        // Add the optional parameters
        ParameterExpression<K> valueParam = null;
        if (value.isPresent()) {
            @SuppressWarnings("unchecked")
            Class<K> valueClass = (Class<K>) value.get().getClass();
            valueParam = cb.parameter(valueClass);
            wherePredicates.add(cb.equal(from.get(TYPE_COLUMN_NAME), valueParam));

        }
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
                                                .setParameter(startDateParam, interval.getStart())
                                                .setParameter(endDateParam, interval.getEnd());
        if (roomName.isPresent()) {
            finalQuery.setParameter(roomNameParam, roomName.get());
        }
        if (username.isPresent()) {
            finalQuery.setParameter(usernameParam, username.get());
        }
        if (value.isPresent()) {
            finalQuery.setParameter(valueParam, value.get());

        }
        return finalQuery.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LabeledDenseMatrix<String> getRoomSimilaritiesByValue(Interval interval) {
        return internalGetSimilaritiesByValue(interval, mention -> mention.getRoomName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LabeledDenseMatrix<String> getUserSimilaritiesByValue(Interval interval) {
        return internalGetSimilaritiesByValue(interval, mention -> mention.getUsername());
    }

    private <X extends Serializable> LabeledDenseMatrix<X>
            internalGetSimilaritiesByValue(Interval interval, Function<T, X> funcX) {
        List<T> mentions = getAllMentions(interval, Optional.absent(), Optional.absent());

        if (mentions.isEmpty()) {
            return LabeledDenseMatrix.of();
        }

        LabeledMatrix<X> M = GraphPartition.getMentionMatrix(mentions,
                                                             funcX,
                                                             mention -> mention.getValue());

        return GraphPartition.getSimilarityMatrix(M);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalMentionsForType(K value, Interval interval, Optional<String> roomName,
                                       Optional<String> username) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        Root<T> from = query.from(type);
        @SuppressWarnings("unchecked")
        ParameterExpression<K> valueParam = (ParameterExpression<K>) cb.parameter(value.getClass());
        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);
        Expression<Integer> sum = cb.sum(from.get("occurrences"));

        query.select(sum.alias("occurrences"));

        List<Predicate> wherePredicates = Lists.newArrayListWithCapacity(4);
        wherePredicates.add(cb.equal(from.get(TYPE_COLUMN_NAME), valueParam));
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

        TypedQuery<Integer> finalQuery = entityManager.createQuery(query)
                                                      .setParameter(valueParam, value)
                                                      .setParameter(startDateParam,
                                                                    interval.getStart())
                                                      .setParameter(endDateParam,
                                                                    interval.getEnd());
        if (roomName.isPresent()) {
            finalQuery.setParameter(roomNameParam, roomName.get());
        }
        if (username.isPresent()) {
            finalQuery.setParameter(usernameParam, username.get());
        }

        List<Integer> result = finalQuery.getResultList();

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
    public Map<K, Long> getTopValuesOfType(Interval interval,
                                           Optional<String> roomName,
                                           Optional<String> username,
                                           int resultSize) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<T> from = query.from(type);
        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);

        Path<K> typeValuePath = from.get(TYPE_COLUMN_NAME);
        Selection<K> typeValueAlias = typeValuePath.alias("typeValue_sel");


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
        Map<K, Long> result = Maps.newLinkedHashMap();
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
