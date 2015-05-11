package com.chatalytics.core.config;

import java.io.Serializable;
import java.util.List;

public class SlackConfig implements Serializable {

    private static final long serialVersionUID = 2857449595685095484L;

    public List<String> authTokens;

    public String baseSlackURL = "https://slack.com/api/";

}
