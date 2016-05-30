package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.LocalTestConfig;
import com.chatalytics.core.util.YamlUtils;
import com.google.common.collect.Maps;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link LocalTestSpout}
 *
 * @author giannis
 */
public class LocalTestSpoutTest {

    private LocalTestSpout underTest;
    private ChatAlyticsConfig config;
    private Map<Object, Object> stormConf;
    private LocalTestConfig chatConfig;

    @Before
    public void setUp() {
        underTest = new LocalTestSpout();
        stormConf = Maps.newHashMapWithExpectedSize(1);
        config = new ChatAlyticsConfig();
        chatConfig = new LocalTestConfig();
        config.computeConfig.chatConfig = chatConfig;
    }

    @Test
    public void testOpen() {
        chatConfig.messageCorpusFile = "test-corpus.txt";
        chatConfig.randomSeed = 0L;
        chatConfig.numRooms = 1;
        chatConfig.numUsers = 2;
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        underTest.open(stormConf, mock(TopologyContext.class), mock(SpoutOutputCollector.class));
        assertEquals(chatConfig.numRooms, underTest.getRooms().size());
        assertEquals(chatConfig.numUsers, underTest.getUsers().size());
    }

    @Test
    public void testOpen_withRandomSeed() {
        chatConfig.messageCorpusFile = "test-corpus.txt";
        chatConfig.randomSeed = null;
        chatConfig.numRooms = 1;
        chatConfig.numUsers = 2;
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        underTest.open(stormConf, mock(TopologyContext.class), mock(SpoutOutputCollector.class));
        assertEquals(chatConfig.numRooms, underTest.getRooms().size());
        assertEquals(chatConfig.numUsers, underTest.getUsers().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOpen_nonExistentFile() {
        chatConfig.messageCorpusFile = "missing-corpus.txt";
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        underTest.open(stormConf, mock(TopologyContext.class), mock(SpoutOutputCollector.class));
    }

    @Test
    public void testNextTuple() {
        chatConfig.messageCorpusFile = "test-corpus.txt";
        chatConfig.randomSeed = 0L;
        chatConfig.sleepMs = 0L;
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        SpoutOutputCollector collector = mock(SpoutOutputCollector.class);
        underTest.open(stormConf, mock(TopologyContext.class), collector);
        underTest.nextTuple();
        verify(collector).emit(any(Values.class));
    }

    @Test
    public void testDeclareOutputFields() {
        OutputFieldsDeclarer fields = mock(OutputFieldsDeclarer.class);
        underTest.declareOutputFields(fields);
        verify(fields).declare(any(Fields.class));
    }
}
