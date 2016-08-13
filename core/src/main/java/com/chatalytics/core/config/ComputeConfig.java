package com.chatalytics.core.config;

import java.io.Serializable;
import java.util.Map;

public class ComputeConfig implements Serializable {

    private static final long serialVersionUID = 2244325546137041102L;

    public String classifier = "classifiers/english.all.3class.distsim.crf.ser.gz";

    public int apiRetries = 3;

    public String apiDateFormat;

    public final String rtComputePath = "/rtcompute";

    public int rtComputePort = 9000;

    public boolean enableRealtimeEvents = true;

    public ChatConfig chatConfig;

    /**
     * Map of property name to file to read
     */
    public Map<String, String> filesToRead;

}
