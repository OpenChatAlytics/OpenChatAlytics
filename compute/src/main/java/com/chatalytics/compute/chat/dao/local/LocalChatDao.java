package com.chatalytics.compute.chat.dao.local;

import com.chatalytics.compute.chat.dao.IChatApiDAO;
import com.chatalytics.core.Constants;
import com.chatalytics.core.RandomStringUtils;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.config.LocalTestConfig;
import com.chatalytics.core.json.JsonObjectMapperFactory;
import com.chatalytics.core.model.data.Message;
import com.chatalytics.core.model.data.Room;
import com.chatalytics.core.model.data.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.io.Resources;

import org.apache.storm.shade.com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LocalChatDao implements IChatApiDAO {

    private final Map<String, User> users;
    private final Map<String, Room> rooms;
    private final ObjectMapper objectMapper;
    private final Map<String, String> emojis;

    public LocalChatDao(ChatAlyticsConfig config) {
        LocalTestConfig localConfig = (LocalTestConfig) config.computeConfig.chatConfig;

        // create the number generator
        long seed;
        if (localConfig.randomSeed == null) {
            seed = System.currentTimeMillis();
        } else {
            seed = localConfig.randomSeed;
        }
        Random rand = new Random(seed);

        this.users = createRandomUsers(localConfig.numUsers, rand);
        this.rooms = createRandomRooms(localConfig.numRooms, rand);
        this.objectMapper = JsonObjectMapperFactory.createObjectMapper(config.inputType);
        this.emojis = createEmojis();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Room> getRooms() {
        return rooms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, User> getUsers() {
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, User> getUsersForRoom(Room room) {
        return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Message> getMessages(DateTime start, DateTime end, Room room) {
        throw new UnsupportedOperationException("Can't get message history from a local chat API");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getEmojis() {
        return emojis;
    }

    /**
     * Creates random users that can be used for generating random messages
     *
     * @param numUsers The number of random users to create
     * @return A map of user ID to random users
     */
    private Map<String, User> createRandomUsers(int numUsers, Random rand) {

        Map<String, User> users = Maps.newHashMapWithExpectedSize(numUsers);

        for (int i = 0; i < numUsers; i++) {
            String userId = RandomStringUtils.generateRandomAlphaNumericString(5, rand);
            String emailId = RandomStringUtils.generateRandomAlphaNumericString(4, rand);
            String email = String.format("%s@email.com", emailId);
            String namePostfix = RandomStringUtils.generateRandomAlphaNumericString(4, rand);
            String name = String.format("name-%s", namePostfix);
            String mentionName = RandomStringUtils.generateRandomAlphaNumericString(6, rand);

            User randomUser = new User(userId, email, false, false, false, name, mentionName, null,
                                       DateTime.now(DateTimeZone.UTC),
                                       DateTime.now(DateTimeZone.UTC), null, null, "UTC", null);

            users.put(userId, randomUser);
        }

        return users;
    }

    /**
     * Creates random rooms that can be used for generating random messages
     *
     * @param numRooms Number of rooms to create
     * @return A map of room ID to random rooms
     */
    private Map<String, Room> createRandomRooms(int numRooms, Random rand) {

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

        return rooms;
    }

    /**
     * A map of emojis to unicode
     *
     * @return A map of emoji names to unicode
     * @throws IOException
     *             When the emoji file can't be found, read or parsed
     */
    private Map<String, String> createEmojis() {
        MapType mapType =  TypeFactory.defaultInstance()
                                      .constructMapType(Map.class, String.class, String.class);
        try {
            return objectMapper.readValue(Resources.getResource(Constants.EMOJI_RESOURCE), mapType);
        } catch (IOException e) {
            throw new RuntimeException("Can't read emojis", e);
        }
    }
}
