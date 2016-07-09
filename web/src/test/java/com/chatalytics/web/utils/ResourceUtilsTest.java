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
}
