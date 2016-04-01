package com.chatalytics.core.model;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * An model object should implement this if it wants to support occurrence statistics
 *
 * @author giannis
 *
 */
public interface IMentionable extends Serializable {

    public String getUsername();

    public String getRoomName();

    public DateTime getMentionTime();

    public int getOccurrences();

}
