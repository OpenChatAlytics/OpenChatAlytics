package com.chatalytics.core.config.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @Tests {@link MissingConfigException}
 *
 * @author giannis
 */
public class MissingConfigExceptionTest {

    @Test
    public void testInit() {
        String msg = "test message";
        assertEquals(msg, new MissingConfigException(msg).getMessage());
    }
}
