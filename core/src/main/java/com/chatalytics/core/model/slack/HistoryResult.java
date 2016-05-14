package com.chatalytics.core.model.slack;

import com.chatalytics.core.model.Message;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

/**
 * Represents the result from a channels.history slack call
 *
 * @author giannis
 */
@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class HistoryResult {

    private final List<Message> messages;
    private final boolean has_more;
    private final boolean ok;
    private final String latest;
}
