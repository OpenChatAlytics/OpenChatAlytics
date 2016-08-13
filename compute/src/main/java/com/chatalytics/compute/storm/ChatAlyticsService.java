package com.chatalytics.compute.storm;

import com.chatalytics.compute.ChatAlyticsEngineMain;
import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.web.realtime.ComputeRealtimeServer;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.util.YamlUtils;
import com.google.common.util.concurrent.AbstractIdleService;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that configures the storm topology and then starts it up. This is started by
 * {@link ChatAlyticsEngineMain}.
 *
 * @author giannis
 *
 */
public class ChatAlyticsService extends AbstractIdleService {

    private static final String TOPOLOGY_NAME = "chat-topology";
    private static final Logger LOG = LoggerFactory.getLogger(ChatAlyticsService.class);

    private final StormTopology chatTopology;
    private LocalCluster cluster;
    private final ChatAlyticsConfig chatalyticsConfig;
    private final ComputeRealtimeServer rtServer;

    public ChatAlyticsService(StormTopology chatTopology,
                              ComputeRealtimeServer rtServer,
                              ChatAlyticsConfig chatalyticsConfig) {
        this.chatTopology = chatTopology;
        this.chatalyticsConfig = chatalyticsConfig;
        this.rtServer = rtServer;
    }

    private LocalCluster submitTopology() throws AlreadyAliveException,
            InvalidTopologyException {
        Config stormConfig = new Config();
        stormConfig.setDebug(false);
        stormConfig.setFallBackOnJavaSerialization(true);
        stormConfig.setNumWorkers(1);

        // enable backpressure since the spouts can move at a much faster speed than the bolts
        stormConfig.put(Config.TOPOLOGY_BACKPRESSURE_ENABLE, true);
        stormConfig.put(Config.TOPOLOGY_EXECUTOR_RECEIVE_BUFFER_SIZE, 64);
        stormConfig.put(Config.TOPOLOGY_EXECUTOR_SEND_BUFFER_SIZE, 64);
        stormConfig.put(Config.TOPOLOGY_SLEEP_SPOUT_WAIT_STRATEGY_TIME_MS, 100);

        stormConfig.setSkipMissingKryoRegistrations(true);
        stormConfig.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt,
                        YamlUtils.writeYaml(chatalyticsConfig));

        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(TOPOLOGY_NAME, stormConfig, chatTopology);
        return cluster;
    }

    @Override
    protected void startUp() throws Exception {
        LOG.info("Starting up...");
        rtServer.startAsync().awaitRunning();
        LOG.info("Submitting storm topology...");
        cluster = submitTopology();
    }

    @Override
    protected void shutDown() throws Exception {
        LOG.info("Shutting down...");
        cluster.killTopology(TOPOLOGY_NAME);
        LOG.info("Waiting a bit for the topology to die...");
        Thread.sleep(2000L);
        LOG.info("Shutting down storm cluster...");
        cluster.shutdown();
        rtServer.stopAsync().awaitTerminated();
    }
}
