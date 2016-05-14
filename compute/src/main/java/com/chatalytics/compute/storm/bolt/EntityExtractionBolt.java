package com.chatalytics.compute.storm.bolt;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IEntityDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.ChatEntity;
import com.chatalytics.core.model.FatMessage;
import com.chatalytics.core.model.Message;
import com.chatalytics.core.model.Room;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import org.apache.storm.shade.com.google.common.collect.ImmutableList;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * This bolt received one message at a time, parses the text of the message and extract entities.
 * Once the entities are extracted and a {@link ChatEntity} object is created, it's persisted to a
 * database.
 *
 * @author giannis
 *
 */
public class EntityExtractionBolt extends ChatAlyticsBaseBolt {

    private static final long serialVersionUID = -1586393277809132608L;
    private static final Logger LOG = LoggerFactory.getLogger(EntityExtractionBolt.class);

    public static final String BOLT_ID = "ENTITY_EXTRACTION_BOLT_ID";
    public static final String CHAT_ENTITY_FIELD_STR = "chat-entity";
    private static final int MAX_ENTITY_CHARS = 150;

    private AbstractSequenceClassifier<CoreLabel> classifier;
    private IEntityDAO entityDao;
    private OutputCollector collector;

    @Override
    public void prepare(ChatAlyticsConfig config, @SuppressWarnings("rawtypes") Map conf,
                        TopologyContext context, OutputCollector collector) {
        classifier = getClassifier(config.computeConfig.classifier);
        entityDao = ChatAlyticsDAOFactory.createEntityDAO(config);
        if (!entityDao.isRunning()) {
            entityDao.startAsync().awaitRunning();
        }
        this.collector = collector;
    }

    /**
     * Gets the classifier to use for parsing text
     *
     * @param config
     *            The configuration object containing information about which classifier to use.
     * @return The classifier to use for extracting entities.
     */
    private AbstractSequenceClassifier<CoreLabel> getClassifier(String classifierStr) {
        URL classifierURL = Resources.getResource(classifierStr);
        AbstractSequenceClassifier<CoreLabel> classifier =
            CRFClassifier.getClassifierNoExceptions(classifierURL.getPath());
        return classifier;
    }

    @Override
    public void execute(Tuple input) {
        LOG.debug("Got tuple: {}", input);
        FatMessage fatMessage = (FatMessage) input.getValue(0);

        List<ChatEntity> entities = extractEntities(fatMessage);

        for (ChatEntity entity : entities) {
            entityDao.persistEntity(entity);
            collector.emit(new Values(entity));
        }
    }

    /**
     * Given a message this method uses a classifier to extract entities.
     *
     * @param fatMessage
     *            The message containing the text to parse.
     * @return A list of entities from the text
     */
    @VisibleForTesting
    protected List<ChatEntity> extractEntities(FatMessage fatMessage) {

        Message message = fatMessage.getMessage();
        String messageStr = message.getMessage();

        if (messageStr == null) {
            return ImmutableList.of();
        }

        List<Triple<String,Integer,Integer>> classification =
                classifier.classifyToCharacterOffsets(messageStr);
        Map<String, ChatEntity> entities = Maps.newHashMapWithExpectedSize(classification.size());

        for (Triple<String, Integer, Integer> triple : classification) {
            if (triple.third - triple.second > MAX_ENTITY_CHARS) {
                continue;
            }
            String entity = messageStr.substring(triple.second, triple.third);
            ChatEntity existingEntity = entities.remove(entity);
            int occurrences;
            if (existingEntity == null) {
                occurrences = 1;
            } else {
                occurrences = existingEntity.getOccurrences() + 1;
            }
            Room room = fatMessage.getRoom();
            String roomName;
            if (room == null) {
                roomName = "";
            } else {
                roomName = room.getName();
            }
            entities.put(entity, new ChatEntity(entity,
                                                occurrences,
                                                message.getDate(),
                                                fatMessage.getUser().getMentionName(),
                                                roomName));

        }

        LOG.debug("Extracted {} entities", entities.size());

        return Lists.newArrayList(entities.values());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(CHAT_ENTITY_FIELD_STR));
    }

    @Override
    public void cleanup() {
        LOG.debug("Cleaning up {}", this.getClass().getSimpleName());
        entityDao.stopAsync().awaitTerminated();
    }

}
