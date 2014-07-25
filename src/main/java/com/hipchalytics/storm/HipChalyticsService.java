package com.hipchalytics.storm;

import com.google.common.util.concurrent.AbstractIdleService;
import com.hipchalytics.HipChalyticsEngineMain;
import com.hipchalytics.config.ConfigurationConstants;
import com.hipchalytics.config.HipChalyticsConfig;
import com.hipchalytics.util.YamlUtils;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;

/**
 * Service that configures the storm topology and then starts it up. This is started by
 * {@link HipChalyticsEngineMain}.
 *
 * @author giannis
 *
 */
public class HipChalyticsService extends AbstractIdleService {

    private final StormTopology hipchatTopology;
    private LocalCluster cluster;
    private final HipChalyticsConfig hipchatConfig;

    public HipChalyticsService(StormTopology hipchatTopology, HipChalyticsConfig hipChatConfig) {
        this.hipchatTopology = hipchatTopology;
        this.hipchatConfig = hipChatConfig;
    }

    private LocalCluster submitTopology() throws AlreadyAliveException,
            InvalidTopologyException {
        Config stormConfig = new Config();
        stormConfig.setDebug(false);
        stormConfig.setFallBackOnJavaSerialization(true);
        stormConfig.setNumWorkers(1);
        stormConfig.setSkipMissingKryoRegistrations(true);
        stormConfig.put(ConfigurationConstants.HIPCHALYTICS_CONFIG.txt,
                        YamlUtils.writeYaml(hipchatConfig));

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("hipchat-topology", stormConfig, hipchatTopology);
        return cluster;
    }

    @Override
    protected void shutDown() throws Exception {
        cluster.shutdown();
    }

    @Override
    protected void startUp() throws Exception {
        cluster = submitTopology();
    }
}
