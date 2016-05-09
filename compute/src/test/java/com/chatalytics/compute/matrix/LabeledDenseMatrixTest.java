package com.chatalytics.compute.matrix;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;

import java.util.ArrayList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link LabeledDenseMatrix}
 *
 * @author giannis
 */
public class LabeledDenseMatrixTest {

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
        LabeledDenseMatrix<String> matrix = LabeledDenseMatrix.of(M, labels);
        assertArrayEquals(Matrices.getArray(M), matrix.getMatrix());
        assertEquals(labels, matrix.getLabels());
    }

    /**
     * Makes sure that the same reference of the singleton empty {@link LabeledDenseMatrix} is
     * returned
     */
    @Test
    public void testCreation_emtpy() {
        LabeledDenseMatrix<String> matrix = LabeledDenseMatrix.of();
        LabeledDenseMatrix<String> otherMatrix = LabeledDenseMatrix.of();
        assertTrue(matrix == otherMatrix);
        assertEquals(0, matrix.getMatrix().length);
        assertEquals(0, matrix.getLabels().size());
    }

    /**
     * Tests to see if a {@link LabeledDenseMatrix} can be created from a {@link LabeledMTJMatrix}
     */
    @Test
    public void testCreation_withLabeledMatrix() {
        LabeledMTJMatrix<String> labeledMatrix = LabeledMTJMatrix.of(M, labels);
        LabeledDenseMatrix<String> matrix = LabeledDenseMatrix.of(labeledMatrix);
        assertArrayEquals(Matrices.getArray(labeledMatrix.getMatrix()), matrix.getMatrix());
        assertEquals(labeledMatrix.getLabels(), matrix.getLabels());
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
