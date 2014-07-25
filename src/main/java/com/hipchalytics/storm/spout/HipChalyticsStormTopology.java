package com.hipchalytics.storm.spout;

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

        return topologyBuilder.createTopology();
    }
}
