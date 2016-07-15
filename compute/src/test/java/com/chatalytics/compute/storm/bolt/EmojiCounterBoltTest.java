package com.chatalytics.compute.storm.bolt;

import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.EmojiEntity;
import com.chatalytics.core.model.data.FatMessage;
import com.chatalytics.core.model.data.Message;
import com.chatalytics.core.model.data.MessageType;
import com.chatalytics.core.model.data.Room;
import com.chatalytics.core.model.data.User;
import com.chatalytics.core.util.YamlUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.TupleImpl;
import org.apache.storm.tuple.Values;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link EmojiCounterBolt}
 * @author giannis
 *
 */
public class EmojiCounterBoltTest {

    private EmojiCounterBolt underTest;
    private User user;
    private Room room;
    private DateTime mentionTime;
    private String emoji;

    @Before
    public void setUp() {
        underTest = new EmojiCounterBolt();
        mentionTime = DateTime.now();
        emoji = "emoji";
        user = new User("randomUserId", "email", false, false, false, null, "randomUserName", null,
                        null, null, null, null, null, null);
        room = new Room("randomRoomId", "randomRoomName", null, null, null, null, false, false,
                        null, null);
    }

    @Test
    public void testExecute() {
        Message message = new Message(mentionTime, "randomFrom", "randomUserId",
                                      String.format("test message with :%s:", emoji),
                                      "randomRoomId", MessageType.MESSAGE);
        FatMessage fatMessage = new FatMessage(message, user, room);
        List<Object> values = Lists.newArrayList(fatMessage);

        TopologyContext context = mock(TopologyContext.class);
        Fields fields = mock(Fields.class);
        when(fields.size()).thenReturn(1);
        when(context.getComponentOutputFields(anyString(), anyString())).thenReturn(fields);
        Tuple input = new TupleImpl(context, values, 0, "stream-id");
        OutputCollector collector = mock(OutputCollector.class);

        ChatAlyticsConfig config = new ChatAlyticsConfig();
        config.computeConfig.apiRetries = 0;
        config.persistenceUnitName = "chatalytics-db-test";
        Map<Object, Object> stormConf = Maps.newHashMapWithExpectedSize(1);
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));

        underTest.prepare(stormConf, context, collector);
        underTest.execute(input);
        verify(collector).emit(any(Values.class));
    }

    @Test
    public void testGetEmojisFromMessage() {
        Message message = new Message(mentionTime, "randomFrom", "randomUserId",
                                      String.format("test message with :%s:", emoji),
                                      "randomRoomId", MessageType.MESSAGE);

        FatMessage fatMessage = new FatMessage(message, user, room);

        List<EmojiEntity> emojis = underTest.getEmojisFromMessage(fatMessage);

        assertEquals(1, emojis.size());
        EmojiEntity firstEmoji = emojis.get(0);
        assertEquals(user.getMentionName(), firstEmoji.getUsername());
        assertEquals(room.getName(), firstEmoji.getRoomName());
        assertEquals(mentionTime, firstEmoji.getMentionTime());
        assertEquals(emoji, firstEmoji.getValue());
        assertEquals(1, firstEmoji.getOccurrences());
    }

    @Test
    public void testGetEmojisFromMessage_noRoom() {
        Message message = new Message(mentionTime, "randomFrom", "randomUserId",
                                      String.format("test message with :%s:", emoji),
                                      "randomRoomId", MessageType.MESSAGE);

        FatMessage fatMessage = new FatMessage(message, user, null);

        List<EmojiEntity> emojis = underTest.getEmojisFromMessage(fatMessage);

        assertEquals(1, emojis.size());
        EmojiEntity firstEmoji = emojis.get(0);
        assertEquals(user.getMentionName(), firstEmoji.getUsername());
        assertNull(firstEmoji.getRoomName());
        assertEquals(mentionTime, firstEmoji.getMentionTime());
        assertEquals(emoji, firstEmoji.getValue());
        assertEquals(1, firstEmoji.getOccurrences());
    }

    @Test
    public void testGetEmojisFromMessage_nullMessage() {
        Message message = new Message(mentionTime, "randomFrom", "randomUserId", null,
                                      "randomRoomId", MessageType.MESSAGE);
        FatMessage fatMessage = new FatMessage(message, user, room);
        List<EmojiEntity> emojis = underTest.getEmojisFromMessage(fatMessage);
        assertTrue(emojis.isEmpty());
    }

    /**
     * Tests to see if multiple occurrences in the same message of the same emoji are correctly
     * recorded
     */
    @Test
    public void testGetEmojisFromMessage_withMultipleOccurrences() {
        Message message = new Message(mentionTime, "randomFrom", "randomUserId",
                                      String.format("test message with :%s::%s: test :%s:",
                                                    emoji, emoji, emoji),
                                      "randomRoomId", MessageType.MESSAGE);

        FatMessage fatMessage = new FatMessage(message, user, room);

        List<EmojiEntity> emojis = underTest.getEmojisFromMessage(fatMessage);

        assertEquals(1, emojis.size());
        EmojiEntity firstEmoji = emojis.get(0);
        assertEquals(user.getMentionName(), firstEmoji.getUsername());
        assertEquals(room.getName(), firstEmoji.getRoomName());
        assertEquals(mentionTime, firstEmoji.getMentionTime());
        assertEquals(emoji, firstEmoji.getValue());
        assertEquals(3, firstEmoji.getOccurrences());
    }

    @Test
    public void testGetEmojisFromMessage_withDanglingColons() {
        Message message = new Message(mentionTime, "randomFrom", "randomUserId",
                                      String.format("test http:// message :     testwith :%s::%s: "
                                          + "test :%s:", emoji, emoji, emoji),
                                      "randomRoomId", MessageType.MESSAGE);

        FatMessage fatMessage = new FatMessage(message, user, room);

        List<EmojiEntity> emojis = underTest.getEmojisFromMessage(fatMessage);

        assertEquals(1, emojis.size());
        EmojiEntity firstEmoji = emojis.get(0);
        assertEquals(user.getMentionName(), firstEmoji.getUsername());
        assertEquals(room.getName(), firstEmoji.getRoomName());
        assertEquals(mentionTime, firstEmoji.getMentionTime());
        assertEquals(emoji, firstEmoji.getValue());
        assertEquals(3, firstEmoji.getOccurrences());
    }

    @Test
    public void testGetEmojisFromMessage_withSpaces() {
        Message message = new Message(mentionTime, "randomFrom", "randomUserId",
                                      String.format("::%s: test :%s:", emoji, emoji),
                                      "randomRoomId", MessageType.MESSAGE);

        FatMessage fatMessage = new FatMessage(message, user, room);

        List<EmojiEntity> emojis = underTest.getEmojisFromMessage(fatMessage);

        assertEquals(1, emojis.size());
        EmojiEntity firstEmoji = emojis.get(0);
        assertEquals(user.getMentionName(), firstEmoji.getUsername());
        assertEquals(room.getName(), firstEmoji.getRoomName());
        assertEquals(mentionTime, firstEmoji.getMentionTime());
        assertEquals(emoji, firstEmoji.getValue());
        assertEquals(2, firstEmoji.getOccurrences());
    }

    @Test
    public void testGetEmojisFromMessage_withJSON() {
        Message message = new Message(mentionTime, "randomFrom", "randomUserId",
                                      "{'test':true,'value':'hello'}",
                                      "randomRoomId", MessageType.MESSAGE);

        FatMessage fatMessage = new FatMessage(message, user, room);

        List<EmojiEntity> emojis = underTest.getEmojisFromMessage(fatMessage);

        assertEquals(0, emojis.size());
    }

    @Test
    public void testDeclareOutputFields() {
        OutputFieldsDeclarer fields = mock(OutputFieldsDeclarer.class);
        underTest.declareOutputFields(fields);
        verify(fields).declare(any(Fields.class));
    }

    @Test
    public void testPrepare() {
        ChatAlyticsConfig config = new ChatAlyticsConfig();
        config.computeConfig.apiRetries = 0;
        config.persistenceUnitName = "chatalytics-db-test";
        Map<Object, Object> stormConf = Maps.newHashMapWithExpectedSize(1);
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));

        TopologyContext context = mock(TopologyContext.class);
        OutputCollector collector = mock(OutputCollector.class);

        underTest.prepare(stormConf, context, collector);
        verifyZeroInteractions(context);
        verifyZeroInteractions(collector);
    }

    @After
    public void tearDown() {
        underTest.cleanup();
    }

}