package com.chatalytics.compute.chat.dao;

import com.chatalytics.compute.chat.dao.hipchat.HipChatApiDAOFactory;
import com.chatalytics.compute.chat.dao.local.LocalChatDao;
import com.chatalytics.compute.chat.dao.slack.SlackApiDAOFactory;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.ChatConfig;
import com.chatalytics.core.config.HipChatConfig;
import com.chatalytics.core.config.LocalTestConfig;
import com.chatalytics.core.config.SlackBackfillerConfig;
import com.chatalytics.core.config.SlackConfig;

/**
 * Helper factory class for creating a chat API DAO
 *
 * @author giannis
 */
public class ChatAPIFactory {

    /**
     * Returns an {@link IChatApiDAO} based on the type of the {@link ChatConfig}
     *
     * @param config
     *            The config to inspect
     * @return An instance of an {@link IChatApiDAO}
     */
    public static IChatApiDAO getChatApiDao(ChatAlyticsConfig config) {
        ChatConfig chatConfig = config.computeConfig.chatConfig;
        if (chatConfig instanceof SlackConfig || chatConfig instanceof SlackBackfillerConfig) {
            return SlackApiDAOFactory.getSlackApiDao(config);
        } else if (chatConfig instanceof HipChatConfig) {
            return HipChatApiDAOFactory.getHipChatApiDao(config);
        } else if (chatConfig instanceof LocalTestConfig) {
            return new LocalChatDao(config);
        } else {
            throw new IllegalArgumentException("Can't determine or get chat API DAO for "
                + chatConfig.getClass().getSimpleName());
        }
    }
}
