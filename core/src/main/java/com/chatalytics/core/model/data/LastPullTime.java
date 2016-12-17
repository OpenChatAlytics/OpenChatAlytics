package com.chatalytics.core.model.data;

import org.joda.time.DateTime;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Class representing the last message pull time
 *
 * @author giannis
 *
 */
@Entity
@Table(name = LastPullTime.LAST_PULL_TIME_TABLE_NAME)
@EqualsAndHashCode
@ToString
public class LastPullTime {

    public static final String LAST_PULL_TIME_TABLE_NAME = "LAST_PULL_TIME";
    private static final String TIME_COLUMN = "TIME";
    // There should only be one value for now
    public final static int ID = 0;

    private DateTime time;

    public LastPullTime() {
    }

    public LastPullTime(DateTime time) {
        this.setTime(time);
    }

    @Column(name = TIME_COLUMN)
    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    @Id
    public int getId() {
        return ID;
    }

    protected void setId(int id) {
        // do nothing
    }

}
