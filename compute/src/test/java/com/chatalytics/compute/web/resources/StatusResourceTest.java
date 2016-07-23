package com.chatalytics.compute.web.resources;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link StatusResource}
 *
 * @author giannis
 */
public class StatusResourceTest {

    private StatusResource underTest;

    @Before
    public void setUp() {
        underTest = new StatusResource();
    }

    @Test
    public void testHealth() {
        String result = underTest.health();
        assertEquals("OK", result);
    }
}
