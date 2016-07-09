package com.chatalytics.compute.matrix;

import com.chatalytics.core.model.data.EmojiEntity;
import com.google.common.collect.ImmutableList;

import org.apache.storm.shade.com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Test;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link GraphPartition}
 *
 * @author giannis
 */
public class GraphPartitionTest {

    public void testGetMentionMatrix() {
        List<EmojiEntity> mentions = Lists.newArrayListWithCapacity(16);
        // make r1, r2 and r2 kind of similar
        mentions.add(new EmojiEntity("u1", "r1", DateTime.now(), "a", 1));
        mentions.add(new EmojiEntity("u1", "r2", DateTime.now(), "a", 1));
        mentions.add(new EmojiEntity("u1", "r3", DateTime.now(), "a", 1));

        mentions.add(new EmojiEntity("u1", "r1", DateTime.now(), "b", 1));
        mentions.add(new EmojiEntity("u1", "r2", DateTime.now(), "b", 1));

        mentions.add(new EmojiEntity("u1", "r2", DateTime.now(), "c", 1));
        mentions.add(new EmojiEntity("u1", "r3", DateTime.now(), "c", 1));

        LabeledMTJMatrix<String> result =
                GraphPartition.getMentionMatrix(mentions,
                                                mention -> mention.getRoomName(),
                                                mention -> mention.getValue());

        // there's three rooms so we expect the size of the matrix to be 3x3 with 3 labels
        assertEquals(3, result.getMatrix().numRows());
        assertEquals(3, result.getMatrix().numColumns());
        assertEquals(3, result.getLabels().stream().distinct().count());
    }

    /**
     * Checks to see if the similarity matrix is computed correctly given <code>M</code>
     */
    @Test
    public void testGetSimilarityMatrix() {
        Matrix M = new DenseMatrix(new double[][] {
            new double[] { 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 },
            new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0 },
            new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0 },
            new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
            new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 },
            new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0 },
            new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 },
            new double[] { 0.0, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0 },
            new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0 },
            new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0 }
        });

        List<String> labels = Lists.newArrayListWithCapacity(M.numRows());
        for (int i = 0; i < M.numRows(); i++) {
            labels.add("L" + i);
        }

        LabeledMTJMatrix<String> M_l = LabeledMTJMatrix.of(M, labels);
        LabeledDenseMatrix<String> R = GraphPartition.getSimilarityMatrix(M_l);
        assertEquals(10, R.getMatrix().length);
        assertEquals(10, R.getMatrix()[0].length);
        assertEquals(10, R.getLabels().size());
    }

    /**
     * Checks to see if a matrix can be permuted correctly given a permutation vector. The
     * permutation is done both on rows and columns
     */
    @Test
    public void testGetPermutationMatrix() {
        Matrix M = new DenseMatrix(new double[][] {
            new double[] { 1, 5, 9,  13 },
            new double[] { 2, 6, 10, 14 },
            new double[] { 3, 7, 11, 15 },
            new double[] { 4, 8, 12, 16 },
        });
        int[] permutations = new int[] { 3, 2, 1, 0 };
        Matrix R = GraphPartition.getPermutedMatrix(M, permutations);
        Matrix R_e = new DenseMatrix(new double[][] {
            new double[] { 16, 12, 8, 4 },
            new double[] { 15, 11, 7, 3 },
            new double[] { 14, 10, 6, 2 },
            new double[] { 13, 9,  5, 1 },
        });

        assertArrayEquals(Matrices.getArray(R_e), Matrices.getArray(R));
    }

    /**
     * Checks to see if the labels are reordered correctly, given a permutation vector
     */
    @Test
    public void testGetPermutedLabels() {
        int[] permutations = new int[] { 2, 1, 3, 0 };
        List<String> originalLabels = ImmutableList.of("e1", "e2", "e3", "e4");
        List<String> result = GraphPartition.getPermutedLabels(originalLabels, permutations);
        List<String> expectedResult = ImmutableList.of("e3", "e2", "e4", "e1");
        assertEquals(expectedResult, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPermutedLabels_withInconsistentLengths() {
        GraphPartition.getPermutedLabels(ImmutableList.of("e1"), new int[] { 2, 1 });
    }

}
