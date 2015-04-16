package com.hipchalytics.model;

import com.google.common.base.MoreObjects;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents an entity extracted from a hipchat message. This is also persisted in the database.
 *
 * @author giannis
 *
 */
@Entity
@Table(name = "entities")
public class HipchatEntity implements Serializable {

    public static final long serialVersionUID = -4845804080646234253L;

    private String entityValue;
    private int occurrences;

    private DateTime mentionTime;

    public HipchatEntity() {
    }

    public HipchatEntity(String entityValue, int occurrences, DateTime mentionTime) {
        this.entityValue = entityValue;
        this.occurrences = occurrences;
        this.mentionTime = mentionTime;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                          .add("entityValue", entityValue)
                          .add("occurrences", occurrences)
                          .add("mentionTime", mentionTime)
                          .toString();
    }

    @Id
    @Column
    public String getEntityValue() {
        return entityValue;
    }

    @Id
    @Column
    public int getOccurrences() {
        return occurrences;
    }

    @Id
    @Column
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    public DateTime getMentionTime() {
        return mentionTime;
    }

    protected void setEntityValue(String entityValue) {
        this.entityValue = entityValue;
    }

    protected void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    protected void setMentionTime(DateTime mentionTime) {
        this.mentionTime = mentionTime;
    }

}
