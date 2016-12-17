package com.chatalytics.core.model.data;

import lombok.Data;

/**
 * Bean that represents a chat message. This bean contains actual {@link Room} and {@link User}
 * objects instead of IDs. The slimmer version of this object is {@link Message}.
 *
 * @author giannis
 *
 */
@Data
public class FatMessage {

    private final Message message;
    private final User user;
    private final Room room;

}
