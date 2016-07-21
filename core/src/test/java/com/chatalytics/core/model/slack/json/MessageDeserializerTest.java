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

    /**
     * Tests deserialize with an attachment message type
     */
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

    /**
     * Tests deserialize with an attachment message type
     */
    @Test
    public void testDeserialize_withShare() throws Exception {
        Message msg = objMapper.readValue(shareStr, Message.class);
        assertEquals("U023BECGF", msg.getFromUserId());
        assertEquals("C0WE24FDS", msg.getRoomId());
        assertEquals("[July 1st, 2016 9:00 PM] user: ms", msg.getMessage());
        // the deserializer drops the nanoseconds
        assertEquals(new DateTime(1464723427000L), msg.getDate());
        assertEquals(null, msg.getFromName());
        assertEquals(MessageType.MESSAGE_SHARE, msg.getType());
    }

    /**
     * Tests deserialize with a message changed type
     */
    @Test
    public void testDeserialize_withMsgChanged() throws Exception {
        Message msg = objMapper.readValue(msgChangedStr, Message.class);
        assertEquals("U8245DFYU", msg.getFromUserId());
        assertEquals("tes", msg.getMessage());
        // the deserializer drops the nanoseconds
        assertEquals(new DateTime(1464743446000L), msg.getDate());
        assertEquals(null, msg.getFromName());
        assertEquals(MessageType.MESSAGE_CHANGED, msg.getType());
    }

    /**
     * Tests deserialize with unknown type
     */
    @Test
    public void testDeserialize_withUnknownType() throws Exception {
        Message msg = objMapper.readValue(unknownMsgStr, Message.class);
        assertEquals("U023BECGF", msg.getFromUserId());
        assertEquals("test message", msg.getMessage());
        // the deserializer drops the nanoseconds
        assertEquals(new DateTime(1431708451000L), msg.getDate());
        assertNull(msg.getFromName());
        assertEquals(MessageType.UNKNOWN, msg.getType());
    }

    /**
     * Tests deserialize with unknown subtype
     */
    @Test
    public void testDeserialize_withUnknownSubtype() throws Exception {
        Message msg = objMapper.readValue(unknownSubtypeMsgStr, Message.class);
        assertEquals("U023BECGF", msg.getFromUserId());
        assertEquals("test message", msg.getMessage());
        // the deserializer drops the nanoseconds
        assertEquals(new DateTime(1431708451000L), msg.getDate());
        assertNull(msg.getFromName());
        assertEquals(MessageType.UNKNOWN, msg.getType());
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

    private final String shareStr = "{" +
                                        "\"type\":\"message\"," +
                                        "\"user\":\"U023BECGF\"," +
                                        "\"text\":\"\"," +
                                        "\"team\":\"T0234DFAA\"," +
                                        "\"user_team\":\"T0234DFAA\"," +
                                        "\"user_profile\":{" +
                                            "\"avatar_hash\":\"61b73f16baff\"," +
                                            "\"image_72\":\"https://image.jpg\"," +
                                            "\"first_name\":\"User\"," +
                                            "\"real_name\":\"User N\"," +
                                            "\"name\":\"user\"" +
                                        "}," +
                                        "\"attachments\":[{" +
                                            "\"fallback\":\"[July 1st, 2016 9:00 PM] user: ms\","+
                                            "\"author_subname\":\"user\"," +
                                            "\"ts\":\"1464723327.000022\"," +
                                            "\"channel_id\":\"C0G1JEKRU\"," +
                                            "\"channel_name\":\"room\"," +
                                            "\"is_msg_unfurl\":true," +
                                            "\"text\":\"test\"," +
                                            "\"author_name\":\"User N\"," +
                                            "\"author_link\":\"https://slack.com/team/user\"," +
                                            "\"author_icon\":\"https://icon.jpg\"," +
                                            "\"mrkdwn_in\":[" +
                                                "\"text\"" +
                                            "]," +
                                            "\"color\":\"D0D0D0\"," +
                                            "\"from_url\":\"https://archives/room/32344\"," +
                                            "\"is_share\":true," +
                                            "\"footer\":\"Posted in #room\"" +
                                        "}]," +
                                        "\"channel\":\"C0WE24FDS\"," +
                                        "\"ts\":\"1464723427.000022\"" +
                                    "}";

    private final String msgChangedStr = "{" +
                                             "\"type\": \"message\"," +
                                             "\"message\": {" +
                                                 "\"type\": \"message\"," +
                                                 "\"user\": \"U8245DFYU\"," +
                                                 "\"text\": \"tes\"," +
                                                 "\"edited\": {" +
                                                     "\"user\": \"U8245DFYU\"," +
                                                     "\"ts\": \"1464743446.000000\"" +
                                                 "}," +
                                                 "\"ts\": \"1464743441.000010\"" +
                                             "}," +
                                             "\"subtype\": \"message_changed\"," +
                                             "\"hidden\": true," +
                                             "\"channel\": \"D0WE24FDS\"," +
                                             "\"previous_message\": {" +
                                                 "\"type\": \"message\"," +
                                                 "\"user\": \"U8245DFYU\"," +
                                                 "\"text\": \"test\"," +
                                                 "\"ts\": \"1464743441.000010\"" +
                                             "}," +
                                             "\"event_ts\": \"1464743446.987192\"," +
                                             "\"ts\":\"1464743446.000011\"" +
                                         "}";

    private final String unknownMsgStr = "{" +
                                             "\"type\": \"uncategorized\"," +
                                             "\"user\": \"U023BECGF\"," +
                                             "\"text\": \"test message\"," +
                                             "\"ts\": \"1431708451.000186\"" +
                                         "}";

    private final String unknownSubtypeMsgStr = "{" +
                                                    "\"type\": \"message\"," +
                                                    "\"subtype\": \"uncategorized\"," +
                                                    "\"user\": \"U023BECGF\"," +
                                                    "\"text\": \"test message\"," +
                                                    "\"ts\": \"1431708451.000186\"" +
                                                "}";

}
