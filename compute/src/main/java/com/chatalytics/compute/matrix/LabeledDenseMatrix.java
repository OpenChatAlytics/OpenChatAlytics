package com.chatalytics.compute.matrix;

import com.google.common.collect.ImmutableList;

import org.apache.storm.shade.com.google.common.base.Preconditions;

import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;

import java.io.Serializable;
import java.util.List;

/**
 * A matrix that stores its elements as a two-dimensional double array.
 *
 * @author giannis
 *
 * @param <L>
 *            The type of the labels
 */
public class LabeledDenseMatrix<L extends Serializable> implements LabeledMatrix<double[][], L> {

    private static LabeledDenseMatrix<Serializable> EMPTY = new LabeledDenseMatrix<>();

    private final double[][] M;
    private final List<L> labels;

    private LabeledDenseMatrix() {
        M = new double[0][0];
        labels = ImmutableList.of();
    }

    private LabeledDenseMatrix(Matrix M, List<L> labels) {
        Preconditions.checkArgument(M.numRows() == labels.size()
                                        || M.numRows() + M.numColumns() == labels.size(),
                                    "The length of the labels is incorrect");
        this.M = Matrices.getArray(M);
        this.labels = labels;
    }

    @Override
    public double[][] getMatrix() {
        return M;
    }

    @Override
    public List<L> getLabels() {
        return labels;
    }

    /**
     * Creates a new {@link LabeledDenseMatrix} by copying the elements of the given matrix to a
     * double two-dimensional array
     *
     * @param M
     *            The matrix. The elements will be copied to a new two-dimensional array
     * @param labels
     *            The matrix labels
     * @return A newly constructed {@link LabeledDenseMatrix}
     */
    public static <L extends Serializable> LabeledDenseMatrix<L> of(Matrix M, List<L> labels) {
        return new LabeledDenseMatrix<>(M, labels);
    }

    /**
     * Creates a new {@link LabeledDenseMatrix} by copying the elements of the given matrix to a
     * double two-dimensional array. Delegates to {@link #of(Matrix, Serializable[])}.
     *
     * @param M
     *            The matrix. The elements will be copied to a new two-dimensional array
     * @param labels
     *            The matrix labels
     * @return A newly constructed {@link LabeledDenseMatrix}
     */
    public static <T extends Serializable> LabeledDenseMatrix<T> of(LabeledMTJMatrix<T> matrix) {
        return of(matrix.getMatrix(), matrix.getLabels());
    }

    /**
     * @return Singleton empty {@link LabeledDenseMatrix}
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> LabeledDenseMatrix<T> of() {
        return (LabeledDenseMatrix<T>) EMPTY;
    }

}
