package com.chatalytics.core.model.data;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * An model object should implement this if it wants to support occurrence statistics
 *
 * @author giannis
 *
 */
public interface IMentionable<T extends Serializable> extends Serializable {

    public T getValue();

    public String getUsername();

    public String getRoomName();

    public DateTime getMentionTime();

    public int getOccurrences();

}
