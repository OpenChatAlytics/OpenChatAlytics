package com.chatalytics.web.utils;

import com.google.common.base.Optional;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link ResourceUtils}
 *
 * @author giannis
 *
 */
public class ResourceUtilsTest {

    @Test
    public void testGetOptionalForParameter() {
        Optional<String> result = ResourceUtils.getOptionalForParameter(null);
        assertEquals(Optional.absent(), result);

        result = ResourceUtils.getOptionalForParameter("");
        assertEquals(Optional.absent(), result);

        String param = "some-param";
        result = ResourceUtils.getOptionalForParameter(param);
        assertEquals(param, result.get());

    }
}
