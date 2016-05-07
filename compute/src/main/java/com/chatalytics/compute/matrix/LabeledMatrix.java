package com.chatalytics.compute.matrix;

import org.apache.storm.shade.com.google.common.base.Preconditions;

import no.uib.cipr.matrix.Matrix;

import java.io.Serializable;
import java.util.List;

/**
 * A matrix that stores its elements by holding a reference to a passed in {@link Matrix}
 *
 * @author giannis
 *
 * @param <L>
 *            The type of the labels
 */
public class LabeledMatrix<L extends Serializable> {

    private final Matrix M;
    private final List<L> labels;

    public LabeledMatrix(Matrix M, List<L> labels) {
        Preconditions.checkArgument(M.numRows() == labels.size()
                                        || M.numRows() + M.numColumns() == labels.size(),
                                    "The length of the labels is incorrect");
        this.M = M;
        this.labels = labels;
    }

    public Matrix getMatrix() {
        return M;
    }

    public List<L> getLabels() {
        return labels;
    }

    /**
     * Returns a new {@link LabeledMatrix}
     *
     * @param M
     *            The Matrix. This matrix will not be copied or converted to an array representation
     * @param labels
     *            The matrix labels
     * @return A newly constructed {@link LabeledMatrix}
     */
    public static <L extends Serializable> LabeledMatrix<L> of(Matrix M, List<L> labels) {
        return new LabeledMatrix<>(M, labels);
    }
}
