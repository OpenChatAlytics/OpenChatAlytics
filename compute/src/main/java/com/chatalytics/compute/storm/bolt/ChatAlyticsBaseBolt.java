package com.chatalytics.compute.storm.bolt;

import com.chatalytics.compute.config.ConfigurationConstants;
import com.chatalytics.core.config.ChatAlyticsConfig;
import com.chatalytics.core.util.YamlUtils;

import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.base.BaseBasicBolt;

import java.util.Map;

/**
 * Inherit from this bolt if you want your prepare method to be called with a
 * {@link ChatAlyticsConfig}
 *
 * @author giannis
 */
public abstract class ChatAlyticsBaseBolt extends BaseBasicBolt {

    private static final long serialVersionUID = -7961960405946887688L;

    @Override
    public void prepare(@SuppressWarnings("rawtypes") Map stormConf, TopologyContext context) {
        String configStr = (String) stormConf.get(ConfigurationConstants.CHATALYTICS_CONFIG.txt);
        ChatAlyticsConfig config = YamlUtils.readChatAlyticsConfigFromString(configStr);
        prepare(config, stormConf, context);
    }

    /**
     * Prepare method to implement that also passes a {@link ChatAlyticsConfig}
     *
     * @param config The {@link ChatAlyticsConfig}
     * @param stormConf The storm config
     * @param context Topology context
     */
    public abstract void prepare(ChatAlyticsConfig config,
                                 @SuppressWarnings("rawtypes") Map stormConf,
                                 TopologyContext context);

}
