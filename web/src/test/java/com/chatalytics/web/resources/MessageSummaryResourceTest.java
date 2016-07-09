package com.chatalytics.web.resources;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IMessageSummaryDAO;
import com.chatalytics.core.ActiveMethod;
import com.chatalytics.core.DimensionType;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.model.data.MessageType;
import com.chatalytics.web.utils.DateTimeUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link MessageSummaryResource}
 *
 * @author giannis
 */
public class MessageSummaryResourceTest {

    private ChatAlyticsConfig config;
    private MessageSummaryResource underTest;
    private IMessageSummaryDAO messageSummaryDAO;
    private DateTimeZone dtZone;
    private DateTime mentionTime;
    private List<MessageSummary> sums;

    @Before
    public void setUp() {
        config = new ChatAlyticsConfig();
        config.persistenceUnitName = "chatalytics-web-test";
        config.timeZone = "America/New_York";
        dtZone = DateTimeZone.forID(config.timeZone);
        messageSummaryDAO = ChatAlyticsDAOFactory.createMessageSummaryDAO(config);
        mentionTime = DateTime.now().withZone(DateTimeZone.UTC);

        sums = Lists.newArrayList();
        sums.add(new MessageSummary("u1", "r1", mentionTime.minus(1), MessageType.BOT_MESSAGE, 1));
        sums.add(new MessageSummary("u2", "r1", mentionTime.minus(2), MessageType.MESSAGE, 1));
        sums.add(new MessageSummary("u2", "r2", mentionTime.minus(3), MessageType.CHANNEL_JOIN, 1));
        sums.add(new MessageSummary("u3", "r1", mentionTime.minus(4), MessageType.MESSAGE, 1));
        sums.add(new MessageSummary("u3", "r2", mentionTime.minus(5), MessageType.CHANNEL_JOIN, 1));
        sums.add(new MessageSummary("u3", "r3", mentionTime.minus(6), MessageType.PINNED_ITEM, 1));
        storeTestMessageSummaries(sums);

        underTest = new MessageSummaryResource(config);
    }

    @Test
    public void testGetAllMessageTypes() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        List<MessageSummary> result = underTest.getAllMessageSummaries(startTimeStr, endTimeStr,
                                                                       null, null, null);
        assertEquals(sums.size(), result.size());

        Set<MessageSummary> resultMessageSummarySet = Sets.newHashSet(result);
        for (MessageSummary expectedMessageSummary : sums) {
            assertTrue(resultMessageSummarySet.contains(expectedMessageSummary));
        }

        result = underTest.getAllMessageSummaries(startTimeStr, endTimeStr, null, null,
                                                  MessageType.BOT_MESSAGE.toString());
        assertEquals(1, result.size());
        assertEquals(MessageType.BOT_MESSAGE, result.get(0).getValue());
    }

    @Test
    public void testGetTotalMessageSummaries() {

        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        int result = underTest.getTotalMessageSummaries(startTimeStr, endTimeStr, null, null, null);
        assertEquals(sums.size(), result);

        result = underTest.getTotalMessageSummaries(startTimeStr, endTimeStr,
                                                    ImmutableList.of("u1"), null, null);
        assertEquals(1, result);

        result = underTest.getTotalMessageSummaries(startTimeStr, endTimeStr, null,
                                                    ImmutableList.of("r1"), null);
        assertEquals(3, result);

        result = underTest.getTotalMessageSummaries(startTimeStr, endTimeStr, null, null,
                                                    MessageType.MESSAGE.toString());
        assertEquals(2, result);
    }

    @Test
    public void testGetActive_userToTV() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Double> scores = underTest.getActive(startTimeStr, endTimeStr,
                                                         DimensionType.USER.toString(),
                                                         ActiveMethod.ToTV.toString(), "10");

        assertFalse(scores.isEmpty());
    }

    @Test
    public void testGetActive_roomToTV() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Double> scores = underTest.getActive(startTimeStr, endTimeStr,
                                                         DimensionType.ROOM.toString(),
                                                         ActiveMethod.ToTV.toString(), "10");

        assertFalse(scores.isEmpty());
    }

    @Test
    public void testGetActive_userTOmV() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Double> scores = underTest.getActive(startTimeStr, endTimeStr,
                                                         DimensionType.USER.toString(),
                                                         ActiveMethod.ToMV.toString(), "10");
        assertFalse(scores.isEmpty());
    }

    @Test
    public void testGetActive_roomToMV() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        Map<String, Double> scores = underTest.getActive(startTimeStr, endTimeStr,
                                                         DimensionType.ROOM.toString(),
                                                         ActiveMethod.ToMV.toString(), "10");
        assertFalse(scores.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetActive_invalidDimension() throws Exception {
        DateTimeFormatter dtf = DateTimeUtils.PARAMETER_WITH_DAY_DTF.withZone(dtZone);
        String startTimeStr = dtf.print(mentionTime.minusDays(1));
        String endTimeStr = dtf.print(mentionTime.plusDays(1));
        underTest.getActive(startTimeStr, endTimeStr, DimensionType.EMOJI.toString(),
                                ActiveMethod.ToTV.toString(), "10");
    }

    @After
    public void tearDown() throws Exception {
        EntityManager em = ChatAlyticsDAOFactory.getEntityManagerFactory(config)
                                                .createEntityManager();
        em.getTransaction().begin();
        em.createNativeQuery("DELETE FROM " + MessageSummary.MESSAGE_SUMMARY_TABLE_NAME)
                .executeUpdate();
        em.getTransaction().commit();
        messageSummaryDAO.stopAsync().awaitTerminated();
    }

    private void storeTestMessageSummaries(List<MessageSummary> summaries) {
        for (MessageSummary messageSummary : summaries) {
            messageSummaryDAO.persistMessageSummary(messageSummary);
        }
    }

}
