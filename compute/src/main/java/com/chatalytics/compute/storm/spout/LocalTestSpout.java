package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.compute.util.YamlUtils;
import com.chatalytics.core.RandomStringUtils;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.LocalTestConfig;
import com.chatalytics.core.model.FatMessage;
import com.chatalytics.core.model.Message;
import com.chatalytics.core.model.Room;
import com.chatalytics.core.model.User;
import com.google.common.collect.Lists;

import org.apache.storm.shade.com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This spout will
 *
 * @author giannis
 */
public class LocalTestSpout extends BaseRichSpout {

    private static final long serialVersionUID = -5167245105191236751L;
    private static final Logger LOG = LoggerFactory.getLogger(LocalTestSpout.class);
    public static final String SPOUT_ID = "LOCAL_TEST_SPOUT_ID";
    public static final String LOCAL_TEST_MESSAGE_FIELD_STR = "test-message";

    private SpoutOutputCollector collector;
    private long sleepMs;
    private Random rand;
    private DateTimeZone dtZone;
    private List<User> users;
    private List<Room> rooms;
    private List<String> sentences;

    @Override
    public void nextTuple() {

        User fromUser = users.get(rand.nextInt(users.size()));
        Room room = rooms.get(rand.nextInt(rooms.size()));

        String messageStr = sentences.get(rand.nextInt(sentences.size()));

        Message message = new Message(DateTime.now(dtZone), fromUser.getName(),
                                      fromUser.getUserId(), messageStr, room.getRoomId());

        FatMessage fatMessage = new FatMessage(message, fromUser, room);

        collector.emit(new Values(fatMessage));

        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted. Ignoring and moving on...");
        }
    }

    @Override
    public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
            SpoutOutputCollector collector) {

        String configYaml = (String) conf.get(ConfigurationConstants.CHATALYTICS_CONFIG.txt);
        ChatAlyticsConfig config = YamlUtils.readYamlFromString(configYaml,
                                                                ChatAlyticsConfig.class);

        LOG.info("Loaded config...");

        LocalTestConfig localConfig = config.computeConfig.localTestConfig;

        this.sleepMs = localConfig.sleepMs;
        this.collector = collector;
        this.dtZone = DateTimeZone.forID(config.timeZone);

        String filename = localConfig.messageCorpusFile;
        try {
            URL corpusURL = ClassLoader.getSystemResource(filename);
            if (corpusURL == null) {
                throw new IllegalArgumentException("Can't find corpus. Specified: " + filename);
            }
            this.sentences = Files.readAllLines(Paths.get(corpusURL.toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new IllegalArgumentException("Can't read corpus. Specified: " + filename, e);
        }

        // create the number generator
        long seed;
        if (localConfig.randomSeed == null) {
            seed = System.currentTimeMillis();
        } else {
            seed = localConfig.randomSeed;
        }
        this.rand = new Random(seed);

        this.users = createRandomUsers(localConfig.numUsers, rand);
        this.rooms = createRandomRooms(localConfig.numRooms, rand);
    }

    /**
     * Creates random users that can be used for generating random messages
     *
     * @param numUsers The number of random users to create
     * @return A list of random users
     */
    private List<User> createRandomUsers(int numUsers, Random rand) {

        Map<String, User> users = Maps.newHashMapWithExpectedSize(numUsers);

        for (int i = 0; i < numUsers; i++) {
            String userId = RandomStringUtils.generateRandomAlphaNumericString(5, rand);
            String emailId = RandomStringUtils.generateRandomAlphaNumericString(4, rand);
            String email = String.format("%s@email.com", emailId);
            String namePostfix = RandomStringUtils.generateRandomAlphaNumericString(4, rand);
            String name = String.format("name-%s", namePostfix);
            String mentionName = RandomStringUtils.generateRandomAlphaNumericString(6, rand);

            User randomUser = new User(userId, email, false, false, name, mentionName, null,
                                       DateTime.now(DateTimeZone.UTC),
                                       DateTime.now(DateTimeZone.UTC), null, null, "UTC", null);

            users.put(userId, randomUser);
        }

        return Lists.newArrayList(users.values());
    }

    /**
     * Creates random rooms that can be used for generating random messages
     *
     * @param numRooms Number of rooms to create
     * @return A list of random rooms
     */
    private List<Room> createRandomRooms(int numRooms, Random rand) {

        Map<String, Room> rooms = Maps.newHashMapWithExpectedSize(numRooms);

        for (int i = 0; i < numRooms; i++) {
            String roomId = RandomStringUtils.generateRandomAlphaNumericString(5, rand);
            String roomPostfix = RandomStringUtils.generateRandomAlphaNumericString(5, rand);
            String name = String.format("room-%s", roomPostfix);
            String ownerUserId =RandomStringUtils.generateRandomAlphaNumericString(5, rand);

            Room randomRoom = new Room(roomId, name, "random topic", DateTime.now(DateTimeZone.UTC),
                                       DateTime.now(DateTimeZone.UTC), ownerUserId, false, false,
                                       null, null);

            rooms.put(roomId, randomRoom);
        }

        return Lists.newArrayList(rooms.values());
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(LOCAL_TEST_MESSAGE_FIELD_STR));
    }

}
