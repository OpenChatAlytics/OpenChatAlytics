package com.chatalytics.compute.db.dao;

import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.model.data.MessageType;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.AbstractIdleService;

import org.joda.time.Interval;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManagerFactory;

/**
 * DAO for storing and retrieving {@link MessageSummary} objects
 *
 * @author giannis
 *
 */
public class MessageSummaryDAOImpl extends AbstractIdleService implements IMessageSummaryDAO {

    private final MentionableDAO<MessageType, MessageSummary> occurrenceStatsDAO;

    public MessageSummaryDAOImpl(EntityManagerFactory emf) {
        this.occurrenceStatsDAO = new MentionableDAO<>(emf, MessageSummary.class);
    }

    @Override
    public void persistMessageSummary(MessageSummary messageSummary) {
        try {
            occurrenceStatsDAO.persistValue(messageSummary);
        } catch (EntityExistsException e) {
            MessageSummary existingValue = occurrenceStatsDAO.getValue(messageSummary);
            int newOccurrences = messageSummary.getOccurrences() + existingValue.getOccurrences();
            MessageSummary mergedSummary = new MessageSummary(messageSummary.getUsername(),
                                                              messageSummary.getRoomName(),
                                                              messageSummary.getMentionTime(),
                                                              messageSummary.getValue(),
                                                              newOccurrences);
            occurrenceStatsDAO.mergeValue(mergedSummary);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageSummary getMessageSummary(MessageSummary messageSummary) {
        return occurrenceStatsDAO.getValue(messageSummary);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageSummary> getAllMessageSummariesForType(MessageType type,
                                                              Interval interval,
                                                              Optional<String> roomName,
                                                              Optional<String> username) {
        return occurrenceStatsDAO.getAllMentionsForValue(type, interval, roomName, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MessageSummary> getAllMessageSummaries(Interval interval,
                                                       Optional<String> roomName,
                                                       Optional<String> username) {
        return occurrenceStatsDAO.getAllMentions(interval, roomName, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalMessageSummaries(Interval interval, Optional<String> roomName,
                                        Optional<String> username) {
        return occurrenceStatsDAO.getTotalMentionsOfType(interval, roomName, username);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalMessageSummariesForType(MessageType type,
                                               Interval interval,
                                               Optional<String> roomName,
                                               Optional<String> username) {
        return occurrenceStatsDAO.getTotalMentionsForType(type, interval, roomName, username);
    }

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
