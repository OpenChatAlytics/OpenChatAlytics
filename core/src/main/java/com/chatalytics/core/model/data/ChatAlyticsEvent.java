package com.chatalytics.core.model.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.joda.time.DateTime;

import lombok.Data;
import lombok.Setter;

@Data
public class ChatAlyticsEvent {

    private final DateTime eventTime;
    private final String type;
    private final Object event;

    public ChatAlyticsEvent(DateTime eventTime, String type, Object event) {
        this.eventTime = eventTime;
        this.type = type;
        this.event = event;
        this.clazz = event.getClass();
    }

    @Setter // null out clazz when it leaves the service
    @JsonIgnore
    private Class<?> clazz;
}
