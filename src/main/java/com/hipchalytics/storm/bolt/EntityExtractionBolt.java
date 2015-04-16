package com.hipchalytics.storm.bolt;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hipchalytics.config.ConfigurationConstants;
import com.hipchalytics.config.HipChalyticsConfig;
import com.hipchalytics.db.dao.HipChalyticsDaoFactory;
import com.hipchalytics.db.dao.IHipChalyticsDao;
import com.hipchalytics.model.HipchatEntity;
import com.hipchalytics.model.FatMessage;
import com.hipchalytics.model.Message;
import com.hipchalytics.util.YamlUtils;

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

import java.net.URL;
import java.util.List;
import java.util.Map;

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

    private List<HipchatEntity> extractEntities(Message message) {
        List<HipchatEntity> entities = Lists.newArrayList();
        List<List<CoreLabel>> results = classifier.classify(message.getMessage());
        LOG.info("DEBUG:" + results);
        return entities;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(HIPCHAT_ENTITY_FIELD_STR));
    }

}
