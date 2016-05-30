package com.chatalytics.compute.storm.bolt;

import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.ChatEntity;
import com.chatalytics.core.model.FatMessage;
import com.chatalytics.core.model.Message;
import com.chatalytics.core.model.MessageType;
import com.chatalytics.core.model.Room;
import com.chatalytics.core.model.User;
import com.chatalytics.core.util.YamlUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.TupleImpl;
import org.apache.storm.tuple.Values;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link EntityExtractionBolt}.
 *
 * @author giannis
 *
 */
public class EntityExtractionBoltTest {

    private EntityExtractionBolt underTest;
    private User user;
    private Room room;
    private TopologyContext context;
    private OutputCollector collector;

    @Before
    public void setUp() throws Exception {
        underTest = new EntityExtractionBolt();


        ChatAlyticsConfig config = new ChatAlyticsConfig();
        config.computeConfig.apiRetries = 0;
        config.persistenceUnitName = "chatalytics-db-test";
        Map<Object, Object> stormConf = Maps.newHashMapWithExpectedSize(1);
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));

        collector = mock(OutputCollector.class);
        context = mock(TopologyContext.class);
        underTest.prepare(stormConf, context, collector);

        Fields fields = mock(Fields.class);
        when(fields.size()).thenReturn(1);
        when(context.getComponentOutputFields(anyString(), anyString())).thenReturn(fields);

        user = new User("randomUserId", "email", false, false, false, null, "randomUserName", null,
                        null, null, null, null, null, null);
        room = new Room("randomRoomId", "randomRoomName", null, null, null, null, false, false,
                        null, null);
    }

    @Test
    public void testExecute() {
        String ent1 = "Jane Doe";
        String ent2 = "Mount Everest";
        Message msg = new Message(DateTime.now(), "jane", "u1",
                                  String.format("Today, %s is going to climb %s", ent1, ent2),
                                  "r1", MessageType.MESSAGE);
        FatMessage fatMessage = new FatMessage(msg, user, room);
        List<Object> values = Lists.newArrayList(fatMessage);
        Tuple input = new TupleImpl(context, values, 0, "stream-id");

        underTest.execute(input);

        verify(collector, times(2)).emit(any(Values.class));
    }

    /**
     * Ensures that entities are properly extracted and returned from a {@link Message}.
     */
    @Test
    public void testExtractEntities() {
        DateTime date = DateTime.now().withZone(DateTimeZone.UTC);
        String mentionName = "jane";
        String userId = "1";
        String roomId = "100";
        String ent1 = "Jane Doe";
        String ent2 = "Mount Everest";
        Message msg = new Message(date, mentionName, userId,
                                  String.format("Today, %s is going to climb %s", ent1, ent2),
                                  roomId, MessageType.MESSAGE);
        FatMessage fatMessage = new FatMessage(msg, user, room);
        List<ChatEntity> entities = underTest.extractEntities(fatMessage);
        Map<String, ChatEntity> entitiesMap = Maps.newHashMapWithExpectedSize(entities.size());
        entities.forEach((entity) -> entitiesMap.put(entity.getValue(), entity));
        assertEquals(2, entities.size());

        ChatEntity entity = entitiesMap.get(ent1);
        assertEquals(ent1, entity.getValue());
        assertEquals(1, entity.getOccurrences());
        assertEquals(date, entity.getMentionTime());

        entity = entitiesMap.get(ent2);
        assertEquals(ent2, entity.getValue());
        assertEquals(1, entity.getOccurrences());
        assertEquals(date, entity.getMentionTime());
    }

    @Test
    public void testExtractEntities_nullMessage() {
        DateTime date = DateTime.now().withZone(DateTimeZone.UTC);
        Message msg = new Message(date, "jane", "1", null, "100", MessageType.MESSAGE);
        User mockUser = mock(User.class);
        when(mockUser.getMentionName()).thenReturn("jane");
        Room mockRoom = mock(Room.class);
        when(mockRoom.getName()).thenReturn("theroom");
        FatMessage fatMessage = new FatMessage(msg, mockUser, mockRoom);
        List<ChatEntity> result = underTest.extractEntities(fatMessage);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testExtractEntities_doubleOccurrence() {
        String mentionName = "jane";
        String userId = "1";
        String roomId = "100";
        String ent = "Mount Everest";
        Message msg = new Message(DateTime.now(), mentionName, userId,
                                  String.format("Today, I'm going to climb %s and %s", ent, ent),
                                  roomId, MessageType.MESSAGE);
        FatMessage fatMessage = new FatMessage(msg, user, room);
        List<ChatEntity> entities = underTest.extractEntities(fatMessage);
        assertEquals(1, entities.size());
        assertEquals(2, entities.get(0).getOccurrences());
    }

    @Test
    public void testExtractEntities_nullRoom() {
        String mentionName = "jane";
        String userId = "1";
        String roomId = "100";
        String ent = "Mount Everest";
        Message msg = new Message(DateTime.now(), mentionName, userId,
                                  String.format("Today, I'm going to climb %s", ent),
                                  roomId, MessageType.MESSAGE);
        FatMessage fatMessage = new FatMessage(msg, user, null);
        List<ChatEntity> entities = underTest.extractEntities(fatMessage);
        assertEquals(1, entities.size());
        assertNull(entities.get(0).getRoomName());
    }

    @After
    public void tearDown() throws Exception {
        underTest.cleanup();
    }
}
