package com.chatalytics.compute.matrix;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link LabeledMTJMatrix}
 *
 * @author giannis
 *
 */
public class LabeledMTJMatrixTest {

    private Matrix M;
    private ArrayList<String> labels;

    @Before
    public void setUp() {
        M = new DenseMatrix(new double[][] {
            new double[] { 1, 2, 3 },
            new double[] { 4, 5, 6 },
            new double[] { 7, 8, 9 },
        });
        labels = Lists.newArrayList("L1", "L2", "L3");
    }

    /**
     * Tests simple creation
     */
    @Test
    public void testCreation() {
        LabeledMTJMatrix<String> matrix = LabeledMTJMatrix.of(M, labels);
        assertTrue(M == matrix.getMatrix());
        assertEquals(labels, matrix.getLabels());
    }

    /**
     * Pass incorrectly sized labels to make sure that matrix creation fails
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreation_withIncorrectSizes() {
        labels.remove(0);
        LabeledDenseMatrix.of(M, labels);
    }
}
