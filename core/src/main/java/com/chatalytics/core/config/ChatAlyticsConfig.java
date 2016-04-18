package com.chatalytics.core.config;

import com.chatalytics.core.InputSourceType;

import java.io.Serializable;

/**
 * The configuration object. Fields of this object are serialized and put in the storm configuration
 * map object. The fields in this object are public and are set through a YAML file found in the
 * resources path.
 *
 * @author giannis
 *
 */
public class ChatAlyticsConfig implements Serializable {

    private static final long serialVersionUID = -1251758543444208166L;

    public InputSourceType inputType;

    public String timeZone = "America/New_York";

    public String persistenceUnitName = "chatalytics-db";

    public WebConfig webConfig = new WebConfig();

    public ComputeConfig computeConfig = new ComputeConfig();

}
