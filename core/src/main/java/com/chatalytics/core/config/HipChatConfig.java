package com.chatalytics.core.config;

import java.io.Serializable;
import java.util.List;

public class HipChatConfig implements Serializable {

    private static final long serialVersionUID = -6648260488672146737L;

    public List<String> authTokens;

    public String baseHipChatURL = "https://api.hipchat.com/v1/";
}
