package com.chatalytics.core.config.exception;

/**
 * Thrown when a config resource cannot be found
 *
 * @author giannis
 *
 */
public class MissingConfigException extends RuntimeException {

    private static final long serialVersionUID = 1462906165636392805L;

    public MissingConfigException(String msg) {
        super(msg);
    }
}
