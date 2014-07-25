package com.hipchalytics.config;

import java.io.Serializable;
import java.util.List;

/**
 * The HipChalytics configuration object. Fields of this object are serialized and put in the storm
 * configuration map object. The fields in this object are public and are set through a YAML file
 * found in the resources path.
 *
 * @author giannis
 *
 */
public class HipChalyticsConfig implements Serializable {

    private static final long serialVersionUID = -1251758543444208166L;

    public List<String> authTokens;

    public String baseHipChatURL = "https://api.hipchat.com/v1/";

    public String apiDateFormat;

    public String timeZone;

}
