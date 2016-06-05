package com.chatalytics.compute.chat.dao;

import com.chatalytics.compute.chat.dao.hipchat.JsonHipChatDAO;
import com.chatalytics.compute.chat.dao.slack.JsonSlackDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.HipChatConfig;
import com.chatalytics.core.config.LocalTestConfig;
import com.chatalytics.core.config.SlackBackfillerConfig;
import com.chatalytics.core.config.SlackConfig;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests {@link ChatAPIFactory}
 *
 * @author giannis
 *
 */
public class ChatAPIFactoryTest {

    /**
     * Checks to see if the right instance of an {@link IChatApiDAO} is returned
     */
    @Test
    public void testGetChatApiDao() {
        ChatAlyticsConfig config = new ChatAlyticsConfig();
        config.computeConfig.apiDateFormat = "YYYY-MM-dd";

        config.computeConfig.chatConfig = new SlackConfig();
        assertTrue(ChatAPIFactory.getChatApiDao(config) instanceof JsonSlackDAO);

        config.computeConfig.chatConfig = new SlackBackfillerConfig();
        assertTrue(ChatAPIFactory.getChatApiDao(config) instanceof JsonSlackDAO);

        config.computeConfig.chatConfig = new HipChatConfig();
        assertTrue(ChatAPIFactory.getChatApiDao(config) instanceof JsonHipChatDAO);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetChatApiDao_invalidType() {
        ChatAlyticsConfig config = new ChatAlyticsConfig();
        config.computeConfig.chatConfig = new LocalTestConfig();
        ChatAPIFactory.getChatApiDao(config);
    }

}
