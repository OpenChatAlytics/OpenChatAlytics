package com.chatalytics.compute.storm.spout;

import com.chatalytics.compute.chat.dao.ChatAPIFactory;
import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.LocalTestConfig;
import com.chatalytics.core.model.data.FatMessage;
import com.chatalytics.core.model.data.Message;
import com.chatalytics.core.model.data.MessageType;
import com.chatalytics.core.model.data.Room;
import com.chatalytics.core.model.data.User;
import com.chatalytics.core.util.YamlUtils;

import org.apache.storm.shade.com.google.common.collect.Lists;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private IChatApiDAO localChatDao;

    @Override
    public void open(@SuppressWarnings("rawtypes") Map conf, TopologyContext context,
                     SpoutOutputCollector collector) {

        String configYaml = (String) conf.get(ConfigurationConstants.CHATALYTICS_CONFIG.txt);
        ChatAlyticsConfig config = YamlUtils.readChatAlyticsConfigFromString(configYaml);

        LOG.info("Loaded config...");

        LocalTestConfig localConfig = (LocalTestConfig) config.computeConfig.chatConfig;

        this.sleepMs = localConfig.sleepMs;
        this.collector = collector;
        this.dtZone = DateTimeZone.forID(config.timeZone);

        String filename = localConfig.messageCorpusFile;

        URL corpusURL = ClassLoader.getSystemResource(filename);
        if (corpusURL == null) {
            throw new IllegalArgumentException("Can't find corpus. Specified: " + filename);
        }
        try {
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
        this.localChatDao = ChatAPIFactory.getChatApiDao(config);
        this.users = Lists.newArrayList(localChatDao.getUsers().values());
        this.rooms = Lists.newArrayList(localChatDao.getRooms().values());
    }

    @Override
    public void nextTuple() {

        User fromUser = users.get(rand.nextInt(users.size()));
        Room room = rooms.get(rand.nextInt(rooms.size()));

        String messageStr = sentences.get(rand.nextInt(sentences.size()));

        Message message = new Message(DateTime.now(dtZone), fromUser.getName(),
                                      fromUser.getUserId(), messageStr, room.getRoomId(),
                                      MessageType.MESSAGE);

        FatMessage fatMessage = new FatMessage(message, fromUser, room);

        collector.emit(new Values(fatMessage));

        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted. Ignoring and moving on...");
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(LOCAL_TEST_MESSAGE_FIELD_STR));
    }

    protected List<Room> getRooms() {
        return rooms;
    }

    protected List<User> getUsers() {
        return users;
    }

}
