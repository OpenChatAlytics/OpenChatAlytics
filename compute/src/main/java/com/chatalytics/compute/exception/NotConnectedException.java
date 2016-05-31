package com.chatalytics.compute.exception;

/**
 * Thrown when can't connect to a socket or API
 *
 * @author giannis
 */
public class NotConnectedException extends RuntimeException {

    private static final long serialVersionUID = 4850226237659793257L;

    public NotConnectedException(String msg) {
        super(msg);
    }
}
