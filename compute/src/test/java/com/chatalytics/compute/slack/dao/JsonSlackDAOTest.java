package com.chatalytics.compute.slack.dao;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.util.YamlUtils;
import com.chatalytics.core.config.ChatAlyticsConfig;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests {@link JsonSlackDAO}
 *
 * @author giannis
 *
 */
public class JsonSlackDAOTest {

    private IChatApiDAO underTest;

    @Before
    public void setUp() {
        ChatAlyticsConfig config = YamlUtils.readYamlFromResource("chatalytics.yaml",
                                                                  ChatAlyticsConfig.class);
        underTest = SlackApiDAOFactory.getSlackApiDao(config);
    }

    /**
     * Makes sure rooms can be returned
     */
    @Test
    @Ignore // work in progress
    public void testGetRooms() {
        underTest.getRooms();
}

}
