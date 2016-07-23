package com.chatalytics.compute;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.storm.ChatAlyticsService;
import com.chatalytics.compute.storm.ChatAlyticsStormTopology;
import com.chatalytics.compute.web.realtime.ComputeRealtimeServer;
import com.chatalytics.compute.web.realtime.ComputeRealtimeServerFactory;
import com.chatalytics.core.CommonCLIBuilder;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.util.YamlUtils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
                LOG.info("Shutting down ChatAlytics Compute...");
                try {
                    chatalyticsService.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    LOG.error("Shutting down chatalytics service timed out...");
                }
                ChatAlyticsDAOFactory.closeEntityManagerFactory();
            }
        });
    }

}
