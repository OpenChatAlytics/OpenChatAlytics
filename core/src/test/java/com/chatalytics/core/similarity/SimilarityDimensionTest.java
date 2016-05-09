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
        SimilarityDimension simDim = SimilarityDimension.fromDimensionName("user");
        assertEquals(SimilarityDimension.USER, simDim);
        assertEquals("user", simDim.getDimensionName());

        simDim = SimilarityDimension.fromDimensionName("entity");
        assertEquals(SimilarityDimension.ENTITY, simDim);
        assertEquals("entity", simDim.getDimensionName());

        simDim = SimilarityDimension.fromDimensionName("room");
        assertEquals(SimilarityDimension.ROOM, simDim);
        assertEquals("room", simDim.getDimensionName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromDimensionName_withBadName() {
        SimilarityDimension.fromDimensionName("bad name");
    }

}