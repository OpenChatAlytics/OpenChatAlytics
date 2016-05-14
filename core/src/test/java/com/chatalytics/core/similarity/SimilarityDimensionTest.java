package com.chatalytics.core.similarity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link SimilarityDimension}
 *
 * @author giannis
 */
public class SimilarityDimensionTest {

    @Test
    public void testFromDimensionName() {
        String dimName = "user";
        SimilarityDimension simDim = SimilarityDimension.fromDimensionName(dimName);
        assertEquals(SimilarityDimension.USER, simDim);
        assertEquals(dimName, simDim.getDimensionName());
        assertEquals(dimName, simDim.toString());

        dimName = "entity";
        simDim = SimilarityDimension.fromDimensionName(dimName);
        assertEquals(SimilarityDimension.ENTITY, simDim);
        assertEquals(dimName, simDim.getDimensionName());
        assertEquals(dimName, simDim.toString());

        dimName = "room";
        simDim = SimilarityDimension.fromDimensionName(dimName);
        assertEquals(SimilarityDimension.ROOM, simDim);
        assertEquals(dimName, simDim.getDimensionName());
        assertEquals(dimName, simDim.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromDimensionName_withBadName() {
        SimilarityDimension.fromDimensionName("bad name");
    }

    @Test(expected = NullPointerException.class)
    public void testFromDimensionName_withNullArg() {
        SimilarityDimension.fromDimensionName(null);
    }
}