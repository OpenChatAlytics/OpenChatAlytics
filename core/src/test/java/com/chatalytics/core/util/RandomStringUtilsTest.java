package com.chatalytics.core.util;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link RandomStringUtils}
 *
 * @author giannis
 *
 */
public class RandomStringUtilsTest {

    @Test
    public void testGenerateRandomAlphaNumericString() {
        int length = 10;
        String result = RandomStringUtils.generateRandomAlphaNumericString(length);
        assertEquals(length, result.length());

        length = 0;
        result = RandomStringUtils.generateRandomAlphaNumericString(length);
        assertEquals(length, result.length());
    }

    @Test
    public void testGenerateRandomAlphaNumericString_withProvidedRandom() {
        int length = 10;
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            strBuilder.append("a");
        }
        Random rand = mock(Random.class);
        when(rand.nextInt(anyInt())).thenReturn(0);
        String result = RandomStringUtils.generateRandomAlphaNumericString(length, rand);
        assertEquals(length, result.length());
        assertEquals(strBuilder.toString(), result);
    }
}
