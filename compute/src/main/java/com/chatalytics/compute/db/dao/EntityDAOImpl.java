package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.model.data.ChatEntity;
import com.google.common.util.concurrent.AbstractIdleService;

import org.joda.time.Interval;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

/**
 * Implementation of the {@link IEntityDAO} that can store and retrieve entities
 *
 * @author giannis
 *
 */
public class EntityDAOImpl extends AbstractIdleService implements IEntityDAO {

    private final IMentionableDAO<String, ChatEntity> occurrenceStatsDAO;

    public EntityDAOImpl(EntityManagerFactory entityManagerFactory) {
        this.occurrenceStatsDAO = new MentionableDAO<>(entityManagerFactory, ChatEntity.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistEntity(ChatEntity entity) {
        occurrenceStatsDAO.persistValue(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatEntity getEntity(ChatEntity entity) {
        return occurrenceStatsDAO.getValue(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChatEntity> getAllMentionsForEntity(String entity,
                                                    Interval interval,
                                                    List<String> roomNames,
                                                    List<String> usernames) {
        return occurrenceStatsDAO.getAllMentionsForValue(entity, interval, roomNames, usernames);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChatEntity> getAllMentions(Interval interval,
                                           List<String> roomNames,
                                           List<String> usernames,
                                           boolean withBots) {
        return occurrenceStatsDAO.getAllMentions(interval, roomNames, usernames, withBots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalMentionsForEntity(String entity,
                                         Interval interval,
                                         List<String> roomNames,
                                         List<String> usernames,
                                         boolean withBots) {

        return occurrenceStatsDAO.getTotalMentionsForType(entity, interval, roomNames, usernames,
                                                          withBots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getTopEntities(Interval interval,
                                            List<String> roomNames,
                                            List<String> usernames,
                                            int resultSize,
                                            boolean withBots) {

        return occurrenceStatsDAO.getTopValuesOfType(interval, roomNames, usernames, resultSize,
                                                     withBots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LabeledDenseMatrix<String> getRoomSimilaritiesByEntity(Interval interval,
                                                                  boolean withBots) {
        return occurrenceStatsDAO.getRoomSimilaritiesByValue(interval, withBots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LabeledDenseMatrix<String> getUserSimilaritiesByEntity(Interval interval,
                                                                  boolean withBots) {
        return occurrenceStatsDAO.getUserSimilaritiesByValue(interval, withBots);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Double> getActiveUsersByMethod(Interval interval,
                                                      ActiveMethod method,
                                                      int resultSize,
                                                      boolean withBots) {
        if (method == ActiveMethod.ToTV) {
            return occurrenceStatsDAO.getActiveColumnsByToTV("username", interval, resultSize,
                                                             withBots);
        } else if (method == ActiveMethod.ToMV) {
            return occurrenceStatsDAO.getActiveColumnsByToMV("username", interval, resultSize,
                                                             withBots);
        } else {
            throw new UnsupportedOperationException(String.format("Method %s not supported",
                                                                  method));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Double> getActiveRoomsByMethod(Interval interval,
                                                      ActiveMethod method,
                                                      int resultSize,
                                                      boolean withBots) {
        if (method == ActiveMethod.ToTV) {
            return occurrenceStatsDAO.getActiveColumnsByToTV("roomName", interval, resultSize,
                                                             withBots);
        } else if (method == ActiveMethod.ToMV) {
            return occurrenceStatsDAO.getActiveColumnsByToMV("roomName", interval, resultSize,
                                                             withBots);
        } else {
            throw new UnsupportedOperationException(String.format("Method %s not supported",
                                                                  method));
        }
    }

    /**
     * Does nothing
     */
    @Override
    protected void startUp() throws Exception {
    }

    /**
     * Closes the underlying {@link MentionableDAO}.
     */
    @Override
    protected void shutDown() throws Exception {
        occurrenceStatsDAO.close();
    }
}
