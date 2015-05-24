package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.util.YamlUtils;
import com.chatalytics.core.config.ChatAlyticsConfig;

import org.apache.storm.guava.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;

import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Tests the {@link SlackMessageSpout}.
 *
 * @author giannis
 *
 */
public class SlackMessageSpoutTest {

    private SlackMessageSpout underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new SlackMessageSpout();
        SpoutOutputCollector mockCollector = mock(SpoutOutputCollector.class);
        TopologyContext mockContext = mock(TopologyContext.class);
        Map<String, String> stormConf = Maps.newHashMapWithExpectedSize(1);
        ChatAlyticsConfig config =
            YamlUtils.readYamlFromResource("chatalytics.yaml", ChatAlyticsConfig.class);
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        underTest.open(stormConf, mockContext, mockCollector);
    }

    @After
    public void tearDown() throws Exception {
        underTest.close();
    }

    @Test
    public void test() throws Exception {
    }

}
