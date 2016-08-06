package com.chatalytics.core.config;

/**
 * Config for backfilling
 *
 * @author giannis
 */
public class SlackBackfillerConfig extends SlackConfig {

    private static final long serialVersionUID = -5343654594296381527L;

    /**
     * How often, in minutes, to run the backfiller
     */
    public int granularityMins;

    /**
     * The backfilling start date. Supports up to millisecond granularity. The format is ISO 8601.
     */
    public String startDate;

    /**
     * Optional end date if you want the backfiller to stop emitting messages beyond this date
     */
    public String endDate;
}
