package com.chatalytics.compute.db.dao;

import com.chatalytics.compute.matrix.LabeledDenseMatrix;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.model.data.ChatEntity;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.AbstractIdleService;

import org.joda.time.Interval;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityExistsException;
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
        try {
            occurrenceStatsDAO.persistValue(entity);
        } catch (EntityExistsException e) {
            ChatEntity existingValue = occurrenceStatsDAO.getValue(entity);
            int newOccurrences = entity.getOccurrences() + existingValue.getOccurrences();
            ChatEntity mergedEntity = new ChatEntity(entity.getValue(), newOccurrences,
                                                     entity.getMentionTime(), entity.getUsername(),
                                                     entity.getRoomName());
            occurrenceStatsDAO.mergeValue(mergedEntity);
        }
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
                                                    Optional<String> roomName,
                                                    Optional<String> username) {
        return occurrenceStatsDAO.getAllMentionsForValue(entity, interval, roomName, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChatEntity> getAllMentions(Interval interval,
                                           Optional<String> roomName,
                                           Optional<String> username) {
        return occurrenceStatsDAO.getAllMentions(interval, roomName, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalMentionsForEntity(String entity,
                                         Interval interval,
                                         Optional<String> roomName,
                                         Optional<String> username) {

        return occurrenceStatsDAO.getTotalMentionsForType(entity, interval, roomName, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> getTopEntities(Interval interval,
                                            Optional<String> roomName,
                                            Optional<String> username,
                                            int resultSize) {

        return occurrenceStatsDAO.getTopValuesOfType(interval, roomName, username, resultSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LabeledDenseMatrix<String> getRoomSimilaritiesByEntity(Interval interval) {
        return occurrenceStatsDAO.getRoomSimilaritiesByValue(interval);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LabeledDenseMatrix<String> getUserSimilaritiesByEntity(Interval interval) {
        return occurrenceStatsDAO.getUserSimilaritiesByValue(interval);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Double> getActiveUsersByMethod(Interval interval,
                                                      ActiveMethod method,
                                                      int resultSize) {
        if (method == ActiveMethod.ToTV) {
            return occurrenceStatsDAO.getActiveColumnsByToTV("username", interval, resultSize);
        } else if (method == ActiveMethod.ToMV) {
            return occurrenceStatsDAO.getActiveColumnsByToMV("username", interval, resultSize);
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
                                                      int resultSize) {
        if (method == ActiveMethod.ToTV) {
            return occurrenceStatsDAO.getActiveColumnsByToTV("roomName", interval, resultSize);
        } else if (method == ActiveMethod.ToMV) {
            return occurrenceStatsDAO.getActiveColumnsByToMV("roomName", interval, resultSize);
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
