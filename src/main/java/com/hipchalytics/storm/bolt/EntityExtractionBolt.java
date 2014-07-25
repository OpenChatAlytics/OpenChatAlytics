package com.hipchalytics.storm.bolt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

import java.util.Map;

public class EntityExtractionBolt extends BaseRichBolt {

    public static final String BOLT_ID = "ENTITY_EXTRACTION_BOLT_ID";
    private static final Logger LOG = LoggerFactory.getLogger(EntityExtractionBolt.class);
    private static final long serialVersionUID = -1586393277809132608L;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        // TODO Auto-generated method stub
    }

    @Override
    public void execute(Tuple input) {
        LOG.info("Got tuple: {}", input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // TODO Auto-generated method stub
    }

}
