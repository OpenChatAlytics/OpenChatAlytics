package com.chatalytics.compute.storm.bolt;

import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.model.FatMessage;
import com.chatalytics.core.model.MessageSummary;

import org.joda.time.DateTime;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

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

    @Override
    public void prepare(ChatAlyticsConfig config, @SuppressWarnings("rawtypes") Map stormConf,
                        TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(Tuple input) {
        FatMessage fatMessage = (FatMessage) input.getValue(0);
        String username = null;
        String roomName = null;
        if (fatMessage.getUser() != null) {
            username = fatMessage.getUser().getMentionName();
        }
        if (fatMessage.getRoom() != null) {
            roomName = fatMessage.getRoom().getName();
        }
        DateTime messageDate = fatMessage.getMessage().getDate();
        MessageSummary chatSummary = new MessageSummary(username, roomName, messageDate);
        collector.emit(new Values(chatSummary));
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer fields) {
        fields.declare(new Fields(MESSAGE_SUMMARY_FIELD_STR));
    }

}