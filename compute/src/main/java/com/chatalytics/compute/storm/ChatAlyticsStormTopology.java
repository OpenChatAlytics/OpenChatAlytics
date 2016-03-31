package com.chatalytics.compute.storm;

import com.chatalytics.compute.storm.bolt.EntityExtractionBolt;
import com.chatalytics.compute.storm.spout.HipChatMessageSpout;
import com.chatalytics.compute.storm.spout.SlackMessageSpout;
import com.chatalytics.core.InputSourceType;

import backtype.storm.generated.StormTopology;
import backtype.storm.topology.TopologyBuilder;

/**
 * Declares and sets up the Storm topology.
 *
 * @author giannis
 *
 */
public class ChatAlyticsStormTopology {

    private final InputSourceType type;

    public ChatAlyticsStormTopology(InputSourceType type) {
        this.type = type;
    }

    public StormTopology get() {
        TopologyBuilder topologyBuilder = new TopologyBuilder();
        String inputSpoutId;
        if (type == InputSourceType.HIPCHAT) {
            inputSpoutId = HipChatMessageSpout.SPOUT_ID;
            topologyBuilder.setSpout(inputSpoutId, new HipChatMessageSpout());
        } else {
            inputSpoutId = SlackMessageSpout.SPOUT_ID;
            topologyBuilder.setSpout(inputSpoutId, new SlackMessageSpout());
        }

        topologyBuilder.setBolt(EntityExtractionBolt.BOLT_ID, new EntityExtractionBolt())
                       .shuffleGrouping(inputSpoutId);

        return topologyBuilder.createTopology();
    }
}
