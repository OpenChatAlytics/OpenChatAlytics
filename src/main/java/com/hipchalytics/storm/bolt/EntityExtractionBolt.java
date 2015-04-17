package com.hipchalytics.storm.bolt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hipchalytics.config.ConfigurationConstants;
import com.hipchalytics.config.HipChalyticsConfig;
import com.hipchalytics.db.dao.HipChalyticsDaoFactory;
import com.hipchalytics.db.dao.IHipChalyticsDao;
import com.hipchalytics.model.FatMessage;
import com.hipchalytics.model.HipchatEntity;
import com.hipchalytics.model.Message;
import com.hipchalytics.util.YamlUtils;

import org.apache.storm.guava.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.XMLUtils;
import edu.stanford.nlp.util.XMLUtils.XMLTag;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * This bolt received one message at a time, parses the text of the message and extract entities.
 * Once the entities are extracted and a {@link HipchatEntity} object is created, it's persisted to
 * a database.
 *
 * @author giannis
 *
 */
public class EntityExtractionBolt extends BaseRichBolt {

    public static final String BOLT_ID = "ENTITY_EXTRACTION_BOLT_ID";
    private static final Logger LOG = LoggerFactory.getLogger(EntityExtractionBolt.class);
    private static final long serialVersionUID = -1586393277809132608L;
    private static final String HIPCHAT_ENTITY_FIELD_STR = "hipchat-entity";

    private AbstractSequenceClassifier<CoreLabel> classifier;
    private IHipChalyticsDao dbDao;

    @Override
    public void prepare(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
            OutputCollector collector) {
        String configYaml = (String) conf.get(ConfigurationConstants.HIPCHALYTICS_CONFIG.txt);
        HipChalyticsConfig hconfig = YamlUtils.readYamlFromString(configYaml,
                                                                  HipChalyticsConfig.class);
        classifier = getClassifier(hconfig);
        dbDao = HipChalyticsDaoFactory.getHipchalyticsDao(hconfig);
    }

    /**
     * Gets the classifier to use for parsing text
     *
     * @param hconfig
     *            The configuration object containing information about which classifier to use.
     * @return The classifier to use for extracting entities.
     */
    private AbstractSequenceClassifier<CoreLabel> getClassifier(HipChalyticsConfig hconfig) {
        URL classifierURL = Resources.getResource(hconfig.classifier);
        AbstractSequenceClassifier<CoreLabel> classifier =
            CRFClassifier.getClassifierNoExceptions(classifierURL.getPath());
        return classifier;
    }

    @Override
    public void execute(Tuple input) {
        LOG.info("Got tuple: {}", input);
        FatMessage fatMessage = (FatMessage) input.getValue(0);
        if (fatMessage == null) {
            LOG.warn("Got a null tuple");
            return;
        }

        List<HipchatEntity> entities = extractEntities(fatMessage.getMessage());
        for (HipchatEntity entity : entities) {
            dbDao.persistEntity(entity);
        }
    }

    /**
     * Given a message this method uses a classifier to extract entities.
     *
     * @param message
     *            The message containing the text to parse.
     * @return A list of entities from the text
     */
    @VisibleForTesting
    protected List<HipchatEntity> extractEntities(Message message) {
        Map<String, HipchatEntity> entities = Maps.newHashMap();
        String messageWithXML = classifier.classifyWithInlineXML(message.getMessage());
        Reader r = new StringReader(messageWithXML);

        try {
            XMLTag tag = XMLUtils.readAndParseTag(r);
            while (tag.name.length() > 0) {
                String entity = XMLUtils.readUntilTag(r);
                if (!tag.isEndTag) {
                    HipchatEntity existingEntity = entities.remove(entity);
                    if (existingEntity == null) {
                        entities.put(entity, new HipchatEntity(entity, 1, message.getDate()));
                    }  else {
                        entities.put(entity, new HipchatEntity(entity,
                                                               existingEntity.getOccurrences() + 1,
                                                               message.getDate()));
                    }
                }

                tag = XMLUtils.readAndParseTag(r);
                if (tag == null || tag.name.length() == 0) {
                    break;
                }
            }

        } catch (IOException e) {

        }
        return Lists.newArrayList(entities.values());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(HIPCHAT_ENTITY_FIELD_STR));
    }

}
