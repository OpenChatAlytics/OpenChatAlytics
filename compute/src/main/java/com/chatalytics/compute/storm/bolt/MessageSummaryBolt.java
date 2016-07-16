package com.chatalytics.compute.storm.bolt;

import com.chatalytics.compute.db.dao.ChatAlyticsDAOFactory;
import com.chatalytics.compute.db.dao.IMessageSummaryDAO;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.data.FatMessage;
import com.chatalytics.core.model.data.MessageSummary;
import com.chatalytics.core.model.data.MessageType;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * Bolt that simply emits a summarized event for every chat message
 *
 * @author giannis
 */
public class MessageSummaryBolt extends ChatAlyticsBaseBolt {

    private static final long serialVersionUID = 2580435620776513082L;

    public static final String BOLT_ID = "MESSAGE_COUNTER_BOLT_ID";
    private static final String MESSAGE_SUMMARY_FIELD_STR = "message-summary";

    private OutputCollector collector;
    private IMessageSummaryDAO messageSummaryDao;

    @Override
    public void prepare(ChatAlyticsConfig config, @SuppressWarnings("rawtypes") Map stormConf,
                        TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.messageSummaryDao = ChatAlyticsDAOFactory.createMessageSummaryDAO(config);
    }

    @Override
    public void execute(Tuple input) {
        FatMessage fatMessage = (FatMessage) input.getValue(0);
        String username = null;
        String roomName = null;
        boolean isBot = true;
        if (fatMessage.getUser() != null) {
            username = fatMessage.getUser().getMentionName();
            isBot = fatMessage.getUser().isBot();
        }
        if (fatMessage.getRoom() != null) {
            roomName = fatMessage.getRoom().getName();
        }
        DateTime messageDate = fatMessage.getMessage().getDate();
        MessageType type = fatMessage.getMessage().getType();
        MessageSummary chatSummary = new MessageSummary(username, roomName, messageDate, type, 1,
                                                        isBot);
        collector.emit(new Values(chatSummary));
        messageSummaryDao.persistMessageSummary(chatSummary);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(MESSAGE_SUMMARY_FIELD_STR));
    }

}
