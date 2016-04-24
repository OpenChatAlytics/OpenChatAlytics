package com.chatalytics.compute.db.dao;

import com.chatalytics.core.model.ChatEntity;
import com.google.common.base.Optional;
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

    @Override
    protected void startUp() throws Exception {
    }

    /**
     * Closes the entity manager and the entity manager factory
     */
    @Override
    protected void shutDown() throws Exception {
        occurrenceStatsDAO.close();
    }
}
