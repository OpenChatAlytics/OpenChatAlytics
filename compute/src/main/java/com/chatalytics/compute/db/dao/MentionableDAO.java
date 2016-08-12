package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.GraphPartition;
import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.compute.matrix.LabeledMTJMatrix;
import com.chatalytics.core.model.data.IMentionable;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.model.data.MessageType;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
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

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

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

    private final Class<T> type;
    private final EntityManagerFactory entityManagerFactory;

    protected MentionableDAO(EntityManagerFactory entityManagerFactory, Class<T> type) {
        this.type = type;
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistValue(T value) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            entityManager.persist(value);
            transaction.commit();
        } catch (PersistenceException e) {
            if (isEntityAlreadyExists(value)) {
                closeEntityManager(entityManager);
                throw new EntityExistsException(e.getCause());
            }
            LOG.error("Cannot store {}. {}", value, e.getMessage());
        }

        if (transaction.isActive() && transaction.getRollbackOnly()) {
            transaction.rollback();
        }

        closeEntityManager(entityManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getValue(T value) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> query = cb.createQuery(type);
            Predicate[] wherePredicates = new Predicate[5];

            Root<T> from = query.from(type);

            Path<String> username = from.get("username");
            Path<String> roomName = from.get("roomName");
            Path<DateTime> mentionTime = from.get("mentionTime");
            Path<Boolean> isBot = from.get("bot");
            Path<K> type = from.get(TYPE_COLUMN_NAME);

            Expression<Integer> sum = cb.sum(from.get("occurrences")).as(Integer.class);
            query.multiselect(username.alias("username"),
                              roomName.alias("roomName"),
                              mentionTime.alias("mentionTime"),
                              type.alias(TYPE_COLUMN_NAME),
                              sum.alias("occurrences"),
                              isBot.alias("bot"));

            query.groupBy(username, roomName, mentionTime, type, isBot);

            wherePredicates[0] = cb.equal(from.get(TYPE_COLUMN_NAME), value.getValue());
            wherePredicates[1] = cb.equal(from.get("username"), value.getUsername());
            wherePredicates[2] = cb.equal(from.get("roomName"), value.getRoomName());
            wherePredicates[3] = cb.equal(from.get("mentionTime"), value.getMentionTime());
            wherePredicates[4] = cb.equal(from.get("bot"), value.isBot());
            query.where(wherePredicates);

            TypedQuery<T> finalQuery = entityManager.createQuery(query);

            try {
                return finalQuery.getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
        } finally {
            closeEntityManager(entityManager);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> getAllMentionsForValue(K value, Interval interval,
                                          List<String> roomNames, List<String> usernames) {
        return internalGetAllMentionsForValue(Optional.of(value), interval, roomNames, usernames,
                                              true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<T> getAllMentions(Interval interval, List<String> roomNames,
                                  List<String> usernames,
                                  boolean withBots) {
        return internalGetAllMentionsForValue(Optional.absent(), interval, roomNames, usernames,
                                              withBots);
    }

    public List<T> internalGetAllMentionsForValue(Optional<K> value,
                                                  Interval interval,
                                                  List<String> roomNames,
                                                  List<String> usernames,
                                                  boolean withBots) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(type);
        Root<T> from = query.from(type);
        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);

        List<Predicate> wherePredicates = Lists.newArrayListWithCapacity(6);
        Path<DateTime> mentionTime = from.get("mentionTime");
        wherePredicates.add(cb.greaterThanOrEqualTo(mentionTime, startDateParam));
        wherePredicates.add(cb.lessThan(mentionTime, endDateParam));
        if (!withBots) {
            wherePredicates.add(cb.equal(from.get("bot"), withBots));
        }

        // Add the optional parameters
        ParameterExpression<K> valueParam = null;
        if (value.isPresent()) {
            @SuppressWarnings("unchecked")
            Class<K> valueClass = (Class<K>) value.get().getClass();
            valueParam = cb.parameter(valueClass);
            wherePredicates.add(cb.equal(from.get(TYPE_COLUMN_NAME), valueParam));
        }
        if (!roomNames.isEmpty()) {
            In<String> in = cb.in(from.get("roomName"));
            for (String roomName : roomNames) {
                in.value(roomName);
            }
            wherePredicates.add(in);
        }
        if (!usernames.isEmpty()) {
            In<String> in = cb.in(from.get("username"));
            for (String username : usernames) {
                in.value(username);
            }
            wherePredicates.add(in);
        }

        query.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

        try {
            TypedQuery<T> finalQuery =
                    entityManager.createQuery(query)
                                 .setParameter(startDateParam, interval.getStart())
                                 .setParameter(endDateParam, interval.getEnd());
            if (value.isPresent()) {
                finalQuery.setParameter(valueParam, value.get());

            }
            return finalQuery.getResultList();
        } finally {
            closeEntityManager(entityManager);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LabeledDenseMatrix<String> getRoomSimilaritiesByValue(Interval interval,
                                                                 boolean withBots) {
        return internalGetSimilaritiesByValue(interval, mention -> mention.getRoomName(), withBots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LabeledDenseMatrix<String> getUserSimilaritiesByValue(Interval interval,
                                                                 boolean withBots) {
        return internalGetSimilaritiesByValue(interval, mention -> mention.getUsername(), withBots);
    }

    private <X extends Serializable> LabeledDenseMatrix<X>
            internalGetSimilaritiesByValue(Interval interval,
                                           Function<T, X> funcX,
                                           boolean withBots) {
        List<T> mentions = getAllMentions(interval, ImmutableList.of(), ImmutableList.of(),
                                          withBots);

        if (mentions.isEmpty()) {
            return LabeledDenseMatrix.of();
        }

        LabeledMTJMatrix<X> M = GraphPartition.getMentionMatrix(mentions,
                                                                funcX,
                                                                mention -> mention.getValue());

        return GraphPartition.getSimilarityMatrix(M);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalMentionsForType(K value, Interval interval, List<String> roomNames,
                                       List<String> usernames, boolean withBots) {
        return internalGetTotalMentions(interval, Optional.of(value), roomNames, usernames,
                                        withBots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalMentionsOfType(Interval interval,
                                      List<String> roomNames,
                                      List<String> usernames,
                                      boolean withBots) {
        return internalGetTotalMentions(interval, Optional.absent(), roomNames, usernames,
                                        withBots);
    }

    private int internalGetTotalMentions(Interval interval,
                                        Optional<K> value,
                                        List<String> roomNames,
                                        List<String> usernames,
                                        boolean withBots) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        Root<T> from = query.from(type);
        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);
        Expression<Integer> sum = cb.sum(from.get("occurrences"));

        query.select(sum.alias("occurrences"));

        List<Predicate> wherePredicates = Lists.newArrayListWithCapacity(6);

        ParameterExpression<K> valueParam = null;
        if (value.isPresent()) {
            @SuppressWarnings("unchecked")
            Class<K> clazz = (Class<K>) value.get().getClass();
            valueParam = cb.parameter(clazz);
            wherePredicates.add(cb.equal(from.get(TYPE_COLUMN_NAME), valueParam));
        }

        Path<DateTime> mentionTime = from.get("mentionTime");
        wherePredicates.add(cb.greaterThanOrEqualTo(mentionTime, startDateParam));
        wherePredicates.add(cb.lessThan(mentionTime, endDateParam));
        if (!withBots) {
            wherePredicates.add(cb.equal(from.get("bot"), withBots));
        }
        // Add the optional parameters
        if (!roomNames.isEmpty()) {
            In<String> in = cb.in(from.get("roomName"));
            for (String roomName : roomNames) {
                in.value(roomName);
            }
            wherePredicates.add(in);
        }
        if (!usernames.isEmpty()) {
            In<String> in = cb.in(from.get("username"));
            for (String username : usernames) {
                in.value(username);
            }
            wherePredicates.add(in);
        }

        query.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

        try {
            TypedQuery<Integer> finalQuery =
                    entityManager.createQuery(query)
                                 .setParameter(startDateParam, interval.getStart())
                                 .setParameter(endDateParam, interval.getEnd());
            if (value.isPresent()) {
                finalQuery.setParameter(valueParam, value.get());
            }

            List<Integer> result = finalQuery.getResultList();

            if (result == null || result.isEmpty() || result.get(0) == null) {
                return 0;
            } else {
                return result.get(0);
            }
        } finally {
            closeEntityManager(entityManager);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<K, Long> getTopValuesOfType(Interval interval,
                                           List<String> roomNames,
                                           List<String> usernames,
                                           int resultSize,
                                           boolean withBots) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();

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
        List<Predicate> wherePredicates = Lists.newArrayListWithCapacity(6);
        Path<DateTime> mentionTime = from.get("mentionTime");
        wherePredicates.add(cb.greaterThanOrEqualTo(mentionTime, startDateParam));
        wherePredicates.add(cb.lessThan(mentionTime, endDateParam));
        wherePredicates.add(cb.isNotNull(typeValuePath));
        if (!withBots) {
            wherePredicates.add(cb.equal(from.get("bot"), withBots));
        }
        // Add the optional parameters
        if (!roomNames.isEmpty()) {
            In<String> in = cb.in(from.get("roomName"));
            for (String roomName : roomNames) {
                in.value(roomName);
            }
            wherePredicates.add(in);
        }
        if (!usernames.isEmpty()) {
            In<String> in = cb.in(from.get("username"));
            for (String username : usernames) {
                in.value(username);
            }
            wherePredicates.add(in);
        }
        query.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

        query.multiselect(typeValueAlias, occurrencesSumAlias);
        query.groupBy(typeValuePath);
        query.orderBy(cb.desc(occurrencesSum));

        try {
            TypedQuery<Tuple> finalQuery =
                    entityManager.createQuery(query)
                                 .setMaxResults(resultSize)
                                 .setParameter(startDateParam, interval.getStart())
                                 .setParameter(endDateParam, interval.getEnd());

            List<Tuple> resultList = finalQuery.getResultList();

            // linked hashmap to preserve order
            Map<K, Long> result = Maps.newLinkedHashMap();
            for (Tuple tuple : resultList) {
                result.put(tuple.get(typeValueAlias), tuple.get(occurrencesSumAlias));
            }
            return result;
        } finally {
            closeEntityManager(entityManager);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Double> getActiveColumnsByToTV(String columnName, Interval interval,
                                                      int resultSize, boolean withBots) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<T> from = query.from(type);

        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);
        Expression<Double> occurrences = from.get("occurrences").as(Double.class);

        // create total query
        Subquery<Long> totalQuery = query.subquery(Long.class);
        Root<T> totalFrom = totalQuery.from(type);
        totalQuery.select(cb.sum(totalFrom.get("occurrences")));
        Path<DateTime> totalMentionTime = totalFrom.get("mentionTime");

        List<Predicate> wherePredicates = Lists.newArrayListWithCapacity(3);
        wherePredicates.add(cb.greaterThanOrEqualTo(totalMentionTime, startDateParam));
        wherePredicates.add(cb.lessThan(totalMentionTime, endDateParam));
        if (!withBots) {
            wherePredicates.add(cb.equal(totalFrom.get("bot"), withBots));
        }
        totalQuery.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

        // occurrences / total occurrences
        Expression<Double> ratio = cb.quot(cb.sum(occurrences), totalQuery).as(Double.class);

        Path<String> columnPath = from.get(columnName);
        query.multiselect(columnPath, ratio);
        Path<DateTime> mentionTime = from.get("mentionTime");

        wherePredicates = Lists.newArrayListWithCapacity(4);
        wherePredicates.add(cb.greaterThanOrEqualTo(mentionTime, startDateParam));
        wherePredicates.add(cb.lessThan(mentionTime, endDateParam));
        wherePredicates.add(cb.isNotNull(columnPath));
        if (!withBots) {
            wherePredicates.add(cb.equal(from.get("bot"), withBots));
        }

        query.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));
        query.groupBy(columnPath);
        query.orderBy(cb.desc(ratio));

        try {
            List<Tuple> resultList = entityManager.createQuery(query)
                                                  .setMaxResults(resultSize)
                                                  .setParameter(startDateParam, interval.getStart())
                                                  .setParameter(endDateParam, interval.getEnd())
                                                  .getResultList();
            // linked hashmap to preserve order
            Map<String, Double> result = Maps.newLinkedHashMap();
            for (Tuple tuple : resultList) {
                result.put(tuple.get(columnPath), tuple.get(ratio));
            }
            return result;
        } finally {
            closeEntityManager(entityManager);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Double> getActiveColumnsByToMV(String columnName, Interval interval,
                                                      int resultSize, boolean withBots) {

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<T> from = query.from(type);

        ParameterExpression<DateTime> startDateParam = cb.parameter(DateTime.class);
        ParameterExpression<DateTime> endDateParam = cb.parameter(DateTime.class);
        Expression<Double> occurrences = from.get("occurrences").as(Double.class);

        // create total query
        Subquery<Long> totalQuery = query.subquery(Long.class);
        Root<MessageSummary> totalFrom = totalQuery.from(MessageSummary.class);
        totalQuery.select(cb.sum(totalFrom.get("occurrences")));
        Path<DateTime> totalMentionTime = totalFrom.get("mentionTime");
        Path<MessageType> messageType = totalFrom.get("value");

        List<Predicate> wherePredicates = Lists.newArrayListWithCapacity(4);
        wherePredicates.add(cb.greaterThanOrEqualTo(totalMentionTime, startDateParam));
        wherePredicates.add(cb.lessThan(totalMentionTime, endDateParam));
        wherePredicates.add(cb.equal(messageType, MessageType.MESSAGE));
        if (!withBots) {
            wherePredicates.add(cb.equal(totalFrom.get("bot"), withBots));
        }
        totalQuery.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

        // occurrences / total occurrences
        Expression<Double> ratio = cb.quot(cb.sum(occurrences), totalQuery).as(Double.class);
        Path<String> columnPath = from.get(columnName);

        query.multiselect(columnPath, ratio);
        Path<DateTime> mentionTime = from.get("mentionTime");

        wherePredicates = Lists.newArrayListWithCapacity(4);
        wherePredicates.add(cb.greaterThanOrEqualTo(mentionTime, startDateParam));
        wherePredicates.add(cb.lessThan(mentionTime, endDateParam));
        wherePredicates.add(cb.isNotNull(columnPath));
        if (!withBots) {
            wherePredicates.add(cb.equal(from.get("bot"), withBots));
        }
        query.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

        query.groupBy(columnPath);
        query.orderBy(cb.desc(ratio));

        try {
            List<Tuple> resultList = entityManager.createQuery(query)
                                                  .setMaxResults(resultSize)
                                                  .setParameter(startDateParam, interval.getStart())
                                                  .setParameter(endDateParam, interval.getEnd())
                                                  .getResultList();

            // linked hashmap to preserve order
            Map<String, Double> result = Maps.newLinkedHashMap();
            for (Tuple tuple : resultList) {
                result.put(tuple.get(columnPath), tuple.get(ratio));
            }
            return result;
        } finally {
            closeEntityManager(entityManager);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public void close() { }

    /**
     * Closes the entity manager
     */
    private void closeEntityManager(EntityManager entityManager) {
        if (!entityManager.isOpen()) {
            return;
        }
        try {
            if (entityManager.isJoinedToTransaction()) {
                entityManager.flush();
            }
        } catch (RuntimeException e) {
            LOG.warn("Couldn't flush entity manager. Reason: {}", e.getMessage());
        }
        try {
            entityManager.close();
        } catch (RuntimeException e) {
            LOG.warn("Couldn't close entity manager. Reason: {}", e.getMessage());
        }
    }

    /**
     * Helper method that determines whether the exception thrown indicates that the value tried to
     * be stored was an already existing one
     *
     * @param value
     *            The value to check if duplicate
     * @return True if the value already exists, false otherwise
     */
    private boolean isEntityAlreadyExists(T value) {
        return getValue(value) != null;
    }

}
