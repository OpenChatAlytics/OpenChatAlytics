package com.chatalytics.compute;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.realtime.ComputeRealtimeServer;
import com.chatalytics.compute.realtime.ComputeRealtimeServerFactory;
import com.chatalytics.compute.storm.ChatAlyticsService;
import com.chatalytics.compute.storm.ChatAlyticsStormTopology;
import com.chatalytics.core.CommonCLIBuilder;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.util.YamlUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

/**
 * Entry point for running the Storm topology.
 *
 * @author giannis
 *
 */
public class ChatAlyticsEngineMain {

    private static final Logger LOG = LoggerFactory.getLogger(ChatAlyticsEngineMain.class);

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {

        Options opts = CommonCLIBuilder.getCommonOptions();
        CommandLine cli = CommonCLIBuilder.parseOptions(ChatAlyticsEngineMain.class, args, opts);
        String configName = CommonCLIBuilder.getConfigOption(cli);

        LOG.info("Loading config {}", configName);
        ChatAlyticsConfig config = YamlUtils.readChatAlyticsConfig(configName);

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
