package com.chatalytics.core;

import com.chatalytics.core.ActiveMethod;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link ActiveMethod}
 *
 * @author giannis
 */
public class ActiveMethodTest {

    @Test
    public void testFromMethodName() {
        String method = "totv";
        ActiveMethod activeMethod = ActiveMethod.fromMethodName(method);
        assertEquals(ActiveMethod.ToTV, activeMethod);
        assertEquals(method, activeMethod.getMethod());
        assertEquals(method, activeMethod.toString());

        method = "tomv";
        activeMethod = ActiveMethod.fromMethodName(method);
        assertEquals(ActiveMethod.ToMV, activeMethod);
        assertEquals(method, activeMethod.getMethod());
        assertEquals(method, activeMethod.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromMethodName_withBadName() {
        ActiveMethod.fromMethodName("bad name");
    }

    @Test(expected = NullPointerException.class)
    public void testFromMethodName_withNullArg() {
        ActiveMethod.fromMethodName(null);
    }
}
