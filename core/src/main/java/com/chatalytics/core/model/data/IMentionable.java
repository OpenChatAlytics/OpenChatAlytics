package com.chatalytics.core.model.data;

import org.joda.time.DateTime;

/**
 * An model object should implement this if it wants to support occurrence statistics
 *
 * @author giannis
 *
 */
public interface IMentionable<T> {

    public T getValue();

    public String getUsername();

    public String getRoomName();

    public DateTime getMentionTime();

    public boolean isBot();

    public int getOccurrences();

}
