package com.chatalytics.core.config;

/**
 * Cofig for backfilling
 *
 * @author giannis
 */
public class BackfillerConfig {

    /**
     * How often, in minutes, to run the backfiller
     */
    public int granularityMins;

    /**
     * The backfilling start date. Supports up to millisecond granularity. The format is ISO 8601.
     */
    public String startDate;

}
