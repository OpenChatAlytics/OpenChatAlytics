package com.hipchalytics;

import com.hipchalytics.config.HipChalyticsConfig;
import com.hipchalytics.storm.HipChalyticsService;
import com.hipchalytics.storm.HipChalyticsStormTopology;
import com.hipchalytics.util.YamlUtils;

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
