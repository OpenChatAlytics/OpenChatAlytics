package com.chatalytics.compute;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.realtime.ComputeRealtimeServer;
import com.chatalytics.compute.realtime.ComputeRealtimeServerFactory;
import com.chatalytics.compute.storm.ChatAlyticsService;
import com.chatalytics.compute.storm.ChatAlyticsStormTopology;
import com.chatalytics.compute.util.YamlUtils;
import com.chatalytics.core.config.ChatAlyticsConfig;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

/**
 * Entry point for running the Storm topology.
 *
 * @author giannis
 *
 */
public class ChatAlyticsEngineMain {

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        ChatAlyticsConfig config = YamlUtils.readYamlFromResource("chatalytics.yaml",
                                                                  ChatAlyticsConfig.class);

        ChatAlyticsStormTopology chatTopology = new ChatAlyticsStormTopology(config.inputType);

        ComputeRealtimeServer rtServer =
            ComputeRealtimeServerFactory.createComputeRealtimeServer(config);
        ChatAlyticsService chatalyticsService = new ChatAlyticsService(chatTopology.get(),
                                                                       rtServer,
                                                                       config);

        addShutdownHook(chatalyticsService);
        chatalyticsService.startAsync().awaitRunning();
    }

    /**
     * Closes all open resources
     *
     * @param chatalyticsService The chatalytics service to close
     */
    public static void addShutdownHook(ChatAlyticsService chatalyticsService) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                chatalyticsService.stopAsync().awaitTerminated();
                ChatAlyticsDAOFactory.closeEntityManagerFactory();
            }
        });
    }

}
