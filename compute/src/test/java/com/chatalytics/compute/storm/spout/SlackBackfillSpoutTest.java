package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.db.dao.IChatAlyticsDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.SlackBackfillerConfig;
import com.chatalytics.core.model.data.FatMessage;
import com.chatalytics.core.model.data.Message;
import com.chatalytics.core.model.data.MessageType;
import com.chatalytics.core.model.data.Room;
import com.chatalytics.core.model.data.User;
import com.chatalytics.core.util.YamlUtils;
import com.google.common.collect.Maps;

import org.apache.storm.shade.com.google.common.collect.ImmutableMap;
import org.apache.storm.shade.com.google.common.collect.Lists;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link SlackBackfillSpout}
 *
 * @author giannis
 */
public class SlackBackfillSpoutTest {

    private SlackBackfillSpout underTest;
    private Map<Object, Object> stormConf;
    private ChatAlyticsConfig config;
    private SlackBackfillerConfig chatConfig;
    private TopologyContext context;
    private SpoutOutputCollector collector;

    @Before
    public void setUp() {
        underTest = new SlackBackfillSpout();
        stormConf = Maps.newHashMapWithExpectedSize(1);
        config = new ChatAlyticsConfig();
        config.persistenceUnitName = "chatalytics-db-test";
        chatConfig = new SlackBackfillerConfig();
        chatConfig.authTokens = Lists.newArrayList("0");
        config.computeConfig.chatConfig = chatConfig;
        context = mock(TopologyContext.class);
        collector = mock(SpoutOutputCollector.class);
    }

    @Test
    public void testOpen() {
        chatConfig.granularityMins = 1;
        chatConfig.startDate = DateTime.now().toString();
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        underTest.open(stormConf, context, collector);
        verifyZeroInteractions(collector, context);
    }

    @Test
    public void testOpen_nullStartDate() {
        chatConfig.granularityMins = 1;
        chatConfig.startDate = null;
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        underTest.open(stormConf, context, collector);
        verifyZeroInteractions(collector, context);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOpen_invalidGranularity() {
        chatConfig.granularityMins = -1;
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        underTest.open(stormConf, context, collector);
    }

    @Test
    public void testNextTuple() {
        chatConfig.granularityMins = 0;
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        IChatAlyticsDAO dbDao = mock(IChatAlyticsDAO.class);
        IChatApiDAO slackDao = mock(IChatApiDAO.class);
        underTest.open(chatConfig, slackDao, dbDao, context, collector);

        Map<String, User> users = ImmutableMap.of("u1", new User("u1", "email", false, false, false,
                                                                 "name", "mention_name", null,
                                                                 DateTime.now(), DateTime.now(),
                                                                 null, null, null, null));
        Map<String, Room> rooms = ImmutableMap.of("r1", new Room("r1", "room", null, DateTime.now(),
                                                                 DateTime.now(), null, false, false,
                                                                 null, null));

        when(dbDao.getLastMessagePullTime()).thenReturn(new DateTime(0, DateTimeZone.UTC));
        when(slackDao.getUsers()).thenReturn(users);
        when(slackDao.getRooms()).thenReturn(rooms);

        underTest.nextTuple();
        verify(slackDao).getUsers();
        verify(slackDao).getRooms();
        verify(dbDao).setLastMessagePullTime(any(DateTime.class));
        verify(dbDao).getLastMessagePullTime();
        verifyNoMoreInteractions(dbDao);
    }

    /**
     * This test makes sure that backfilling doesn't run when the latest pull time is after the
     * current time
     */
    @Test
    public void testNextTuple_outsideOfInterval() {
        chatConfig.granularityMins = 0;
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        IChatAlyticsDAO dbDao = mock(IChatAlyticsDAO.class);
        IChatApiDAO slackDao = mock(IChatApiDAO.class);
        underTest.open(chatConfig, slackDao, dbDao, context, collector);
        when(dbDao.getLastMessagePullTime()).thenReturn(DateTime.now(DateTimeZone.UTC).plusDays(2));

        underTest.nextTuple();
        verify(dbDao).getLastMessagePullTime();
        verifyNoMoreInteractions(dbDao);
        verifyZeroInteractions(slackDao);
    }

    /**
     * Check to see if a {@link FatMessage} gets emitted when the user is not null, the message type
     * is valid and the room is not archived
     */
    @Test
    public void testBackfillRooms() {
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        IChatAlyticsDAO dbDao = mock(IChatAlyticsDAO.class);
        IChatApiDAO slackDao = mock(IChatApiDAO.class);
        underTest.open(chatConfig, slackDao, dbDao, context, collector);

        Map<String, User> users = ImmutableMap.of("u1", new User("u1", "email", false, false, false,
                                                                 "name", "mention_name", null,
                                                                 DateTime.now(), DateTime.now(),
                                                                 null, null, null, null));
        Room room = new Room("r1", "room", null, DateTime.now(), DateTime.now(), null, false, false,
                             null, null);
        Map<String, Room> rooms = ImmutableMap.of("r1", room);
        Interval interval = new Interval(DateTime.now().minusDays(1), DateTime.now());
        Message message = new Message(DateTime.now(), "from", "u1", "test message", "r1",
                                      MessageType.MESSAGE);
        List<Message> messages = Lists.newArrayList(message);

        when(slackDao.getMessages(interval.getStart(), interval.getEnd(), room)).thenReturn(messages);

        underTest.backfillRooms(users, rooms, interval);
        verify(slackDao).getMessages(interval.getStart(), interval.getEnd(), room);
        verifyNoMoreInteractions(slackDao);
        verify(collector).emit(any(Values.class));
        verifyNoMoreInteractions(collector);
    }

    @Test
    public void testBackfillRooms_archivedRoom() {
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        IChatAlyticsDAO dbDao = mock(IChatAlyticsDAO.class);
        IChatApiDAO slackDao = mock(IChatApiDAO.class);
        underTest.open(chatConfig, slackDao, dbDao, context, collector);

        Map<String, User> users = ImmutableMap.of("u1", new User("u1", "email", false, false, false,
                                                                 "name", "mention_name", null,
                                                                 DateTime.now(), DateTime.now(),
                                                                 null, null, null, null));
        Map<String, Room> rooms = ImmutableMap.of("r1", new Room("r1", "room", null, DateTime.now(),
                                                                 DateTime.now(), null, true, false,
                                                                 null, null));
        Interval interval = new Interval(DateTime.now().minusDays(1), DateTime.now());

        underTest.backfillRooms(users, rooms, interval);
        verifyZeroInteractions(collector, slackDao);
    }

    @Test
    public void testBackfillRooms_unknownMessageType() {
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        IChatAlyticsDAO dbDao = mock(IChatAlyticsDAO.class);
        IChatApiDAO slackDao = mock(IChatApiDAO.class);
        underTest.open(chatConfig, slackDao, dbDao, context, collector);

        Map<String, User> users = ImmutableMap.of("u1", new User("u1", "email", false, false, false,
                                                                 "name", "mention_name", null,
                                                                 DateTime.now(), DateTime.now(),
                                                                 null, null, null, null));
        Room room = new Room("r1", "room", null, DateTime.now(), DateTime.now(), null, false, false,
                             null, null);
        Map<String, Room> rooms = ImmutableMap.of("r1", room);
        Interval interval = new Interval(DateTime.now().minusDays(1), DateTime.now());
        Message message = new Message(DateTime.now(), "from", "u1", "test message", "r1",
                                      MessageType.UNKNOWN);
        List<Message> messages = Lists.newArrayList(message);

        when(slackDao.getMessages(interval.getStart(), interval.getEnd(), room)).thenReturn(messages);

        underTest.backfillRooms(users, rooms, interval);
        verify(slackDao).getMessages(interval.getStart(), interval.getEnd(), room);
        verifyNoMoreInteractions(slackDao);
        verifyZeroInteractions(collector);
    }

    @Test
    public void testBackfillRooms_nullUser() {
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        IChatAlyticsDAO dbDao = mock(IChatAlyticsDAO.class);
        IChatApiDAO slackDao = mock(IChatApiDAO.class);
        underTest.open(chatConfig, slackDao, dbDao, context, collector);

        Map<String, User> users = ImmutableMap.of("u1", new User("u1", "email", false, false, false,
                                                                 "name", "mention_name", null,
                                                                 DateTime.now(), DateTime.now(),
                                                                 null, null, null, null));
        Room room = new Room("r1", "room", null, DateTime.now(), DateTime.now(), null, false, false,
                             null, null);
        Map<String, Room> rooms = ImmutableMap.of("r1", room);
        Interval interval = new Interval(DateTime.now().minusDays(1), DateTime.now());
        Message message = new Message(DateTime.now(), "from", "u2", "test message", "r1",
                                      MessageType.MESSAGE);
        List<Message> messages = Lists.newArrayList(message);

        when(slackDao.getMessages(interval.getStart(), interval.getEnd(), room)).thenReturn(messages);

        underTest.backfillRooms(users, rooms, interval);
        verify(slackDao).getMessages(interval.getStart(), interval.getEnd(), room);
        verifyNoMoreInteractions(slackDao);
        verifyZeroInteractions(collector);
    }

    @Test
    public void testBackfillRooms_botUser() {
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        IChatAlyticsDAO dbDao = mock(IChatAlyticsDAO.class);
        IChatApiDAO slackDao = mock(IChatApiDAO.class);
        underTest.open(chatConfig, slackDao, dbDao, context, collector);

        Map<String, User> users = ImmutableMap.of("u1", new User("u1", "email", false, false, false,
                                                                 "name", "mention_name", null,
                                                                 DateTime.now(), DateTime.now(),
                                                                 null, null, null, null));
        Room room = new Room("r1", "room", null, DateTime.now(), DateTime.now(), null, false, false,
                             null, null);
        Map<String, Room> rooms = ImmutableMap.of("r1", room);
        Interval interval = new Interval(DateTime.now().minusDays(1), DateTime.now());
        Message message = new Message(DateTime.now(), "from", "b1", "test message", "r1",
                                      MessageType.BOT_MESSAGE);
        List<Message> messages = Lists.newArrayList(message);

        when(slackDao.getMessages(interval.getStart(), interval.getEnd(), room)).thenReturn(messages);

        underTest.backfillRooms(users, rooms, interval);
        verify(slackDao).getMessages(interval.getStart(), interval.getEnd(), room);
        verifyNoMoreInteractions(slackDao);
        verify(collector).emit(any(Values.class));
        verifyNoMoreInteractions(collector);
    }

    @Test
    public void testGetRunInterval_noEndDate() {
        DateTime startDate = DateTime.now(DateTimeZone.UTC).minusDays(1);
        chatConfig.startDate = startDate.toString();
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        IChatAlyticsDAO dbDao = mock(IChatAlyticsDAO.class);
        IChatApiDAO slackDao = mock(IChatApiDAO.class);
        underTest.open(chatConfig, slackDao, dbDao, context, collector);

        // last pull time is in the past
        when(dbDao.getLastMessagePullTime()).thenReturn(DateTime.now().minusDays(100));
        Interval returnInterval = underTest.getRunInterval().get();
        assertEquals(startDate, returnInterval.getStart());
        // approximately now
        assertEquals(DateTime.now(DateTimeZone.UTC).withMillisOfSecond(0),
                     returnInterval.getEnd().withMillisOfSecond(0));
        verify(dbDao).getLastMessagePullTime();
        verifyNoMoreInteractions(dbDao);

        // last pull time is in the future
        reset(dbDao);
        when(dbDao.getLastMessagePullTime()).thenReturn(DateTime.now().plusDays(100));
        assertFalse(underTest.getRunInterval().isPresent());
        verify(dbDao).getLastMessagePullTime();
        verifyNoMoreInteractions(dbDao);

        // init time is before last pull time
        reset(dbDao);
        DateTime lastPullTime = startDate.plusDays(1);
        when(dbDao.getLastMessagePullTime()).thenReturn(lastPullTime);
        returnInterval = underTest.getRunInterval().get();
        assertEquals(lastPullTime, returnInterval.getStart());
        // approximately now
        assertEquals(DateTime.now(DateTimeZone.UTC).withMillisOfSecond(0),
                     returnInterval.getEnd().withMillisOfSecond(0));
        verify(dbDao).getLastMessagePullTime();
        verifyNoMoreInteractions(dbDao);
    }

    @Test
    public void testGetRunInterval_withEndDate() {
        DateTime startDate = DateTime.now(DateTimeZone.UTC).minusDays(1);
        DateTime endDate = startDate.plusHours(3);
        chatConfig.startDate = startDate.toString();
        chatConfig.endDate = endDate.toString();
        chatConfig.granularityMins = 60;
        stormConf.put(ConfigurationConstants.CHATALYTICS_CONFIG.txt, YamlUtils.writeYaml(config));
        IChatAlyticsDAO dbDao = mock(IChatAlyticsDAO.class);
        IChatApiDAO slackDao = mock(IChatApiDAO.class);
        underTest.open(chatConfig, slackDao, dbDao, context, collector);

        // last pull time is in the past
        when(dbDao.getLastMessagePullTime()).thenReturn(DateTime.now().minusDays(100));
        Interval returnInterval = underTest.getRunInterval().get();
        assertEquals(startDate, returnInterval.getStart());
        // this is end date
        assertEquals(endDate.withMillisOfSecond(0), returnInterval.getEnd().withMillisOfSecond(0));
        assertEquals(startDate, returnInterval.getStart());
        verify(dbDao).getLastMessagePullTime();
        verifyNoMoreInteractions(dbDao);

        // last pull time is in the future
        reset(dbDao);
        when(dbDao.getLastMessagePullTime()).thenReturn(DateTime.now().plusDays(100));
        assertFalse(underTest.getRunInterval().isPresent());
        verify(dbDao).getLastMessagePullTime();
        verifyNoMoreInteractions(dbDao);

        // init time is before last pull time but next start is after end time
        reset(dbDao);
        DateTime lastPullTime = startDate.plusDays(1);
        when(dbDao.getLastMessagePullTime()).thenReturn(lastPullTime);
        assertFalse(underTest.getRunInterval().isPresent());
        verify(dbDao).getLastMessagePullTime();
        verifyNoMoreInteractions(dbDao);

        // last run time + granularity is after the end date
        reset(dbDao);
        // a minute after the end date plus granularity
        lastPullTime = endDate.minusMinutes(chatConfig.granularityMins - 1);
        when(dbDao.getLastMessagePullTime()).thenReturn(lastPullTime);
        returnInterval = underTest.getRunInterval().get();
        assertEquals(lastPullTime, returnInterval.getStart());
        assertEquals(endDate, returnInterval.getEnd());
        verify(dbDao).getLastMessagePullTime();
        verifyNoMoreInteractions(dbDao);
    }

    @Test
    public void testDeclareOutputFields() {
        OutputFieldsDeclarer fields = mock(OutputFieldsDeclarer.class);
        underTest.declareOutputFields(fields);
        verify(fields).declare(any(Fields.class));
    }

    @After
    public void tearDown() {
        underTest.close();
    }
}
