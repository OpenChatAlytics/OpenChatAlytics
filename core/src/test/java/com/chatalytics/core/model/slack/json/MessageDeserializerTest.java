package com.chatalytics.core.model.slack.json;

import com.chatalytics.core.model.data.Message;
import com.chatalytics.core.model.data.MessageType;
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
        assertEquals(MessageType.MESSAGE, msg.getType());
    }

    /**
     * Tests to see if a bot message can be properly deserialized
     */
    @Test
    public void testDeserialize_WithBotMessage() throws Exception {
        Message msg = objMapper.readValue(messageBotJsonStr, Message.class);
        assertEquals("B0N8R69KR", msg.getFromUserId());
        assertEquals("a bot message", msg.getMessage());
        // the deserializer drops the nanoseconds
        assertEquals(new DateTime(1431719027010L), msg.getDate());
        assertEquals("bot", msg.getFromName());
        assertEquals(MessageType.BOT_MESSAGE, msg.getType());
    }

    @Test
    public void testDeserialize_withAttachments() throws Exception {
        Message msg = objMapper.readValue(jiraStr, Message.class);
        assertEquals("B0234S4SHT", msg.getFromUserId());
        assertEquals("Change <http://jira.net/TI-5|TI-5>", msg.getMessage());
        // the deserializer drops the nanoseconds
        assertEquals(new DateTime(1464723327000L), msg.getDate());
        assertEquals(null, msg.getFromName());
        assertEquals(MessageType.BOT_MESSAGE, msg.getType());
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

    private final String jiraStr = "{" +
                                       "\"text\": \"\"," +
                                       "\"bot_id\": \"B0234S4SHT\"," +
                                       "\"attachments\": [{" +
                                           "\"fallback\": \"Change <http://jira.net/TI-5|TI-5>\"," +
                                           "\"pretext\": \"Change <http://jira.net/TI-5|TI-5>\"," +
                                           "\"title\": \"Computers needed\"," +
                                           "\"id\": 1," +
                                           "\"title_link\": \"http://jira.net/TI-5\"," +
                                           "\"color\": \"daa038\"," +
                                           "\"fields\": [{" +
                                               "\"title\": \"Priority\"," +
                                               "\"value\": \"Minor\"," +
                                               "\"short\": true" +
                                           "}]" +
                                       "}]," +
                                       "\"type\": \"message\"," +
                                       "\"subtype\": \"bot_message\"," +
                                       "\"ts\": \"1464723327.000002\"" +
                                   "}";

}
