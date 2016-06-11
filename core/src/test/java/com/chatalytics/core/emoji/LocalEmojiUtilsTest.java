package com.chatalytics.core.emoji;

import com.chatalytics.core.json.JsonObjectMapperFactory;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link LocalEmojiUtils}
 *
 * @author giannis
 */
public class LocalEmojiUtilsTest {

    @Test
    public void testGetUnicodeEmojis() {
        ObjectMapper objectMapper = JsonObjectMapperFactory.createObjectMapper();
        Map<String, String> emojis = LocalEmojiUtils.getUnicodeEmojis(objectMapper);
        assertNotNull(emojis);
        assertFalse(emojis.isEmpty());
    }

    @Test
    public void testGetUnicodeEmojis_withException() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        IOException exception = new IOException("test");
        when(objectMapper.readValue(any(URL.class), any(JavaType.class))).thenThrow(exception);
        try {
            LocalEmojiUtils.getUnicodeEmojis(objectMapper);
            fail();
        } catch (RuntimeException e) {
            assertEquals(exception, e.getCause());
        }
    }
}
