package com.chatalytics.core.util;

import com.chatalytics.core.InputSourceType;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.ChatConfig;
import com.chatalytics.core.config.HipChatConfig;
import com.chatalytics.core.config.LocalTestConfig;
import com.chatalytics.core.config.SlackBackfillerConfig;
import com.chatalytics.core.config.SlackConfig;
import com.chatalytics.core.config.exception.MissingConfigException;
import com.chatalytics.core.util.YamlUtils;

import org.junit.Test;
import org.yaml.snakeyaml.constructor.ConstructorException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link YamlUtils}
 *
 * @author giannis
 */
public class YamlUtilsTest {

    /**
     * Writes out a {@link ChatAlyticsConfig} to a String object and then reads it back
     */
    @Test
    public void testReadYamlFromString() {

        ChatAlyticsConfig config = new ChatAlyticsConfig();
        config.inputType = InputSourceType.SLACK;
        SlackConfig slackConfig = new SlackConfig();
        slackConfig.baseAPIURL = "http://test/url";
        config.computeConfig.chatConfig = slackConfig;
        String configStr = YamlUtils.writeYaml(config);

        ChatAlyticsConfig readConfig = YamlUtils.readChatAlyticsConfigFromString(configStr);

        assertEquals(config.inputType, readConfig.inputType);
        assertEquals(config.timeZone, readConfig.timeZone);
        ChatConfig readChatConfig = readConfig.computeConfig.chatConfig;
        assertTrue(readChatConfig instanceof SlackConfig);
        assertEquals(slackConfig.getBaseAPIURL(), readChatConfig.getBaseAPIURL());
    }

    @Test
    public void testReadYamlFromResource() {
        String expectedURL = "http://test/url/from/file";

        String resource = "config/chatalytics-slack.yaml";
        ChatAlyticsConfig readConfig = YamlUtils.readChatAlyticsConfig(resource);
        assertTrue(readConfig.computeConfig.chatConfig instanceof SlackConfig);
        assertEquals(expectedURL, readConfig.computeConfig.chatConfig.getBaseAPIURL());

        resource = "config/chatalytics-hipchat.yaml";
        readConfig = YamlUtils.readChatAlyticsConfig(resource);
        assertTrue(readConfig.computeConfig.chatConfig instanceof HipChatConfig);
        assertEquals(expectedURL, readConfig.computeConfig.chatConfig.getBaseAPIURL());

        resource = "config/chatalytics-local.yaml";
        readConfig = YamlUtils.readChatAlyticsConfig(resource);
        assertTrue(readConfig.computeConfig.chatConfig instanceof LocalTestConfig);

        resource = "config/chatalytics-slackbackfill.yaml";
        readConfig = YamlUtils.readChatAlyticsConfig(resource);
        assertTrue(readConfig.computeConfig.chatConfig instanceof SlackBackfillerConfig);
        assertEquals(expectedURL, readConfig.computeConfig.chatConfig.getBaseAPIURL());

    }

    @Test(expected = MissingConfigException.class)
    public void testReadYamlFromResource_missingResource() {
        YamlUtils.readChatAlyticsConfig("missing-resource");
    }

    @Test(expected = ConstructorException.class)
    public void testReadYamlFromResource_badResource() {
        YamlUtils.readChatAlyticsConfig("config/bad-config.yaml");
    }
}
