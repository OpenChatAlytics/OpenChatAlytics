package com.chatalytics.core.model.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link JsonChatDeserializer}
 *
 * @author giannis
 *
 */
public class JsonChatDeserializerTest {

    private JsonChatDeserializer<String> underTest;

    @Before
    public void setUp() throws Exception {
        underTest = new JsonChatDeserializer<String>() {

            @Override
            public String deserialize(JsonParser jp, DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                // no op
                return null;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Tests to see if a boolean node that could potentially be null can be parsed correctly
     */
    @Test
    public void testGetAsBooleanOrNull() {
        assertFalse(underTest.getAsBooleanOrFalse(null));
        JsonNode mockNode = mock(JsonNode.class);
        when(mockNode.asBoolean()).thenReturn(true);
        assertTrue(underTest.getAsBooleanOrFalse(mockNode));
    }

    /**
     * Tests to see if a text node that could potentially be null can be parsed correctly
     */
    @Test
    public void testGetAsTextOrNull() {
        assertNull(underTest.getAsTextOrNull(null));
        JsonNode mockNode = mock(JsonNode.class);
        String returnValue = "test";
        when(mockNode.asText()).thenReturn(returnValue);
        assertEquals(returnValue, underTest.getAsTextOrNull(mockNode));
    }

}
