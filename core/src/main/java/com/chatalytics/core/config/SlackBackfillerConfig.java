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
     * Optional end date if you want the backfiller to stop emitting messages beyond this date
     */
    public String endDate;
}
