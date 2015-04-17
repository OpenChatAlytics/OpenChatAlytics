package com.hipchalytics.compute;

import com.hipchalytics.compute.config.HipChalyticsConfig;
import com.hipchalytics.compute.storm.HipChalyticsService;
import com.hipchalytics.compute.storm.HipChalyticsStormTopology;
import com.hipchalytics.compute.util.YamlUtils;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;

/**
 * Entry point for running the Storm topology.
 *
 * @author giannis
 *
 */
public class HipChalyticsEngineMain {

    public static void main(String[] args) throws FileNotFoundException, URISyntaxException {
        HipChalyticsStormTopology hipchatTopology = new HipChalyticsStormTopology();
        HipChalyticsConfig config = YamlUtils.readYamlFromResource("hipchat.yaml",
                                                                   HipChalyticsConfig.class);
        HipChalyticsService hipChopilyticsService = new HipChalyticsService(hipchatTopology.get(),
                                                                            config);
        hipChopilyticsService.startAsync().awaitRunning();

    }

}
