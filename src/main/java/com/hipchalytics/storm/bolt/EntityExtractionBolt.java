package com.hipchalytics.storm.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Tuple;

import java.util.Map;

public class EntityExtractionBolt extends BaseRichBolt {

    public static final String BOLT_ID = "ENTITY_EXTRACTION_BOLT_ID";

    private static final long serialVersionUID = -1586393277809132608L;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        // TODO Auto-generated method stub
    }

    @Override
    public void execute(Tuple input) {
        // TODO Auto-generated method stub
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // TODO Auto-generated method stub
    }

}
