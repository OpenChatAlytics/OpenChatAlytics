package com.chatalytics.compute.matrix;

import java.io.Serializable;
import java.util.List;

/**
 *
 * A matrix that's labeled
 *
 * @author giannis
 *
 * @param <T>
 *            The type of the matrix
 * @param <L>
 *            The type of the labels
 */
public interface LabeledMatrix<T, L extends Serializable> {

    /**
     * Gets the list of labels
     *
     * @return The list of labels
     */
    List<L> getLabels();

    /**
     * Gets the matrix
     *
     * @return The matrix
     */
    T getMatrix();

}
