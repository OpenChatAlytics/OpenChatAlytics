package com.chatalytics.core.util;

import java.util.Random;

/**
 * Contains convenience methods for working with random strings
 *
 * @author giannis
 *
 */
public class RandomStringUtils {

    private static final char[] ALPHANUMERIC_CHARACTERS = new char[] { 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
        'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

    /**
     * Returns a random alphanumeric string using a {@link Random} generator with the seed being the
     * current timestamp
     *
     * @param length
     *            The number of characters to generate
     * @return A random alphanumeric strings
     */
    public static String generateRandomAlphaNumericString(int length) {
        return generateRandomAlphaNumericString(length, new Random(System.currentTimeMillis()));
    }

    /**
     * Returns a random alphanumeric string using a specified {@link Random} generator
     *
     * @param length
     *            The number of characters to generate
     * @param rand
     *            The random generator to use
     * @return A random alphanumeric strings
     */
    public static String generateRandomAlphaNumericString(int length, Random rand) {

        char[] charset = new char[length];

        for (int i = 0; i < length; i++) {
            charset[i] = ALPHANUMERIC_CHARACTERS[rand.nextInt(ALPHANUMERIC_CHARACTERS.length)];
        }

        return new String(charset);
    }
}
