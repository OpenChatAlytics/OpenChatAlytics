package com.chatalytics.core.model.slack.json;

import com.chatalytics.core.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link MessageDeserializer} for slack.
 *
 * @author giannis
 */
public class MessageDeserializerTest {

    private ObjectMapper objMapper;

    @Before
    public void setUp() throws Exception {
        objMapper = new ObjectMapper();
        objMapper.registerModule(new SlackJsonModule());
    }

    /**
     * Tests to see if a regular message can be properly deserialized
     */
    @Test
    public void testDeserialize() throws Exception {
        Message msg = objMapper.readValue(messageJsonStr, Message.class);
        assertEquals("U023BECGF", msg.getFromUserId());
        assertEquals("test message", msg.getMessage());
        // the deserializer drops the nanoseconds
        assertEquals(new DateTime(1431708451000L), msg.getDate());
        assertNull(msg.getFromName());
    }

    /**
     * Tests to see if a bot message can be properly deserialized
     */
    @Test
    public void testDeserialize_WithBotMessage() throws Exception {
        Message msg = objMapper.readValue(messageBotJsonStr, Message.class);
        assertEquals("bot", msg.getFromUserId());
        assertEquals("a bot message", msg.getMessage());
        // the deserializer drops the nanoseconds
        assertEquals(new DateTime(1431719027010L), msg.getDate());
        assertEquals("bot", msg.getFromName());
    }

    private final String messageJsonStr = "{" +
                                              "\"type\": \"message\"," +
                                              "\"user\": \"U023BECGF\"," +
                                              "\"text\": \"test message\"," +
                                              "\"ts\": \"1431708451.000186\"" +
                                          "}";

    private final String messageBotJsonStr = "{" +
                                                 "\"text\": \"a bot message\"," +
                                                 "\"username\": \"bot\"," +
                                                 "\"bot_id\": \"B0N8R69KR\"," +
                                                 "\"icons\": {" +
                                                     "\"emoji\": \":boom:\"," +
                                                     "\"image_64\": \"https:\\/\\/image.com\"" +
                                                 "}," +
                                                 "\"type\": \"message\"," +
                                                 "\"subtype\": \"bot_message\", " +
                                                 "\"ts\": \"1431719027.010187\"" +
                                             "}";

}
