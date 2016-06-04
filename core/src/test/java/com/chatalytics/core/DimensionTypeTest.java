package com.chatalytics.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link DimensionType}
 *
 * @author giannis
 */
public class DimensionTypeTest {

    @Test
    public void testFromDimensionName() {
        String dimName = "user";
        DimensionType simDim = DimensionType.fromDimensionName(dimName);
        assertEquals(DimensionType.USER, simDim);
        assertEquals(dimName, simDim.getDimensionName());
        assertEquals(dimName, simDim.toString());

        dimName = "entity";
        simDim = DimensionType.fromDimensionName(dimName);
        assertEquals(DimensionType.ENTITY, simDim);
        assertEquals(dimName, simDim.getDimensionName());
        assertEquals(dimName, simDim.toString());

        dimName = "room";
        simDim = DimensionType.fromDimensionName(dimName);
        assertEquals(DimensionType.ROOM, simDim);
        assertEquals(dimName, simDim.getDimensionName());
        assertEquals(dimName, simDim.toString());

        dimName = "emoji";
        simDim = DimensionType.fromDimensionName(dimName);
        assertEquals(DimensionType.EMOJI, simDim);
        assertEquals(dimName, simDim.getDimensionName());
        assertEquals(dimName, simDim.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromDimensionName_withBadName() {
        DimensionType.fromDimensionName("bad name");
    }

    @Test(expected = NullPointerException.class)
    public void testFromDimensionName_withNullArg() {
        DimensionType.fromDimensionName(null);
    }
}