package com.hipchalytics.storm;

import com.hipchalytics.storm.bolt.EntityExtractionBolt;
import com.hipchalytics.storm.spout.HipChatMessageSpout;

import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;

/**
 * Declares and sets up the Storm topology.
 *
 * @author giannis
 *
 */
public class HipChalyticsStormTopology {

    public HipChalyticsStormTopology() {

    }

    public StormTopology get() {
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        topologyBuilder.setSpout(HipChatMessageSpout.SPOUT_ID, new HipChatMessageSpout());

        topologyBuilder.setBolt(EntityExtractionBolt.BOLT_ID, new EntityExtractionBolt())
            .shuffleGrouping(HipChatMessageSpout.SPOUT_ID);

        return topologyBuilder.createTopology();
    }
}
