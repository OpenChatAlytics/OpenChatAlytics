package com.chatalytics.web.utils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.apache.storm.shade.com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

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

    @Test
    public void testGetListFromNullable() {
        assertEquals(ImmutableList.of(), ResourceUtils.getListFromNullable(null));
        List<String> test = Lists.newArrayList("a", "b");
        assertEquals(test, ResourceUtils.getListFromNullable(test));
    }

    @Test
    public void testGetOptionalForParameterAsInt() {
        Optional<Integer> result = ResourceUtils.getOptionalForParameterAsInt(null);
        assertEquals(Optional.absent(), result);

        result = ResourceUtils.getOptionalForParameterAsInt("");
        assertEquals(Optional.absent(), result);

        int value = 2;
        result = ResourceUtils.getOptionalForParameterAsInt(Integer.toString(value));
        assertEquals(value, result.get().intValue());
    }

    @Test
    public void testGetOptionalForParameterAsBool() {
        Optional<Boolean> result = ResourceUtils.getOptionalForParameterAsBool(null);
        assertEquals(Optional.absent(), result);

        result = ResourceUtils.getOptionalForParameterAsBool("");
        assertEquals(Optional.absent(), result);

        boolean value = false;
        result = ResourceUtils.getOptionalForParameterAsBool(Boolean.toString(value));
        assertEquals(value, result.get().booleanValue());
    }
}
