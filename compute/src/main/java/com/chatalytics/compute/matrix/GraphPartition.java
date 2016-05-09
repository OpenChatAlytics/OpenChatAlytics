package com.chatalytics.compute.matrix;

import com.chatalytics.core.model.IMentionable;

import org.apache.storm.shade.com.google.common.base.Preconditions;
import org.apache.storm.shade.com.google.common.collect.Lists;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.PermutationMatrix;
import no.uib.cipr.matrix.SymmDenseEVD;
import no.uib.cipr.matrix.sparse.LinkedSparseMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Contains functions to compute partitions in a graph using matrices
 *
 * @author giannis
 */
public class GraphPartition {

    /**
     * Given a list of mentions and two functions that extract two field names, call them dimension
     * <code>X</code> and dimension <code>Y</code>, this function will first create a matrix, with
     * rows <code>Y</code> and columns <code>X</code>, with the values representing the number of
     * times <code>X</code> occurred with <code>Y</code>. It will then transpose multiply that
     * matrix with itself to give the final result.
     * <p/>
     * For example:</br>
     * <p/>
     * Given the following data:
     * <pre>
     * A(x<sub>1</sub>, y<sub>1</sub>), B(x<sub>2</sub>, y<sub>3</sub>),
     * C(x<sub>2</sub>, y<sub>4</sub>), D(x<sub>2</sub>, y<sub>3</sub>),
     * E(x<sub>3</sub>, y<sub>1</sub>), F(x<sub>3</sub>, y<sub>4</sub>)
     * </pre>
     * The mention matrix, before the transpose and multiply would look like:
     * <pre>
     *     x<sub>1</sub>  x<sub>2</sub>  x<sub>3</sub>
     * y<sub>1</sub>  1   0   1
     * y<sub>2</sub>  0   0   0
     * y<sub>3</sub>  0   2   0
     * y<sub>4</sub>  0   1   1
     * </pre>
     *
     * @param data
     *            The mentions
     * @param funcR
     *            Function to extract dimension <code>R</code> from an {@link IMentionable}
     * @param funcC
     *            Function to extract dimension <code>C</code> from an {@link IMentionable}
     * @return The similarity matrix, which is a square matrix with the size being the number of
     *         distinct <code>R</code> elements
     */
    public static <T extends IMentionable<? extends Serializable>,
                   X extends Serializable,
                   Y extends Serializable> LabeledMTJMatrix<X>
                           getMentionMatrix(List<T> data,
                                            Function<T, X> funcX,
                                            Function<T, Y> funcY) {
        // row
        List<Y> dimYOrd = data.stream()
                              .map(funcY)
                              .distinct()
                              .collect(Collectors.toList()); // need ordering
        // column
        List<X> dimXOrd = data.stream()
                              .map(funcX)
                              .distinct()
                              .collect(Collectors.toList()); // need ordering

        Map<X, Integer> dimXToIdx = IntStream.range(0, dimXOrd.size())
                                             .boxed()
                                             .collect(Collectors.toMap(i -> dimXOrd.get(i),
                                                                       i -> i));
        Map<Y, Integer> dimYToIdx = IntStream.range(0, dimYOrd.size())
                                             .boxed()
                                             .collect(Collectors.toMap(i -> dimYOrd.get(i),
                                                                       i -> i));
        Matrix M = new LinkedSparseMatrix(dimYOrd.size(), dimXOrd.size());

        for (T mention : data) {
            int rowIdx = dimYToIdx.get(funcY.apply(mention));
            int columnIdx = dimXToIdx.get(funcX.apply(mention));
            double existingValue = M.get(rowIdx, columnIdx);
            M.set(rowIdx, columnIdx, existingValue + mention.getOccurrences());
        }

        Matrix M_c = M.copy();
        Matrix A = M.transAmult(M_c, new DenseMatrix(M.numColumns(), M.numColumns()));

        // make the labels
        @SuppressWarnings("unchecked")
        X[] labels = (X[]) new Serializable[dimXToIdx.size()];
        for (Map.Entry<X, Integer> entry : dimXToIdx.entrySet()) {
            labels[entry.getValue()] = entry.getKey();
        }

        return LabeledMTJMatrix.of(A, Lists.newArrayList(labels));
    }

    /**
     * Computes a similarity matrix based on a given matrix A that represents connections between
     * two dimensions. Based on:
     * <a href=https://www.cs.purdue.edu/homes/dgleich/demos/matlab/spectral/spectral.html>this
     * tutorial</a>
     *
     * @param labeledMatrix
     *            A labeled matrix to compute similarity on
     * @return A re-ordering of the original matrix with all the different clusters partitioned
     *         closely
     */
    public static <L extends Serializable> LabeledDenseMatrix<L> getSimilarityMatrix(
            LabeledMTJMatrix<L> labeledMatrix) {
        Matrix A = labeledMatrix.getMatrix();
        // Build diagonal matrix (D)
        SparseVector V = new SparseVector(A.numColumns());
        A.forEach((entry) -> V.set(entry.row(), V.get(entry.row()) + entry.get()));
        DenseMatrix D = new DenseMatrix(V.size(), V.size());
        for (int row = 0, column = 0; row < V.size(); row++, column++) {
            D.set(row, column, V.get(row));
        }

        // Build Laplace matrix (L = D - A)
        Matrix L = D.add(-1, A);

        // compute eigen vectors
        DenseMatrix E;
        try {
            E = SymmDenseEVD.factorize(L).getEigenvectors();
        } catch (NotConvergedException e) {
            throw new RuntimeException("Can't factorize matrix", e);
        }
        // get the second eigen vector
        DenseVector E_2 = new DenseVector(E.numRows());
        for (int i = 0; i < E.numRows(); i++) {
            E_2.set(i, E.get(i, 1));
        }

        // sort E_2 and store the permutations
        Integer[] indices = new Integer[E_2.size()];
        for (int i = 0; i < E_2.size(); i++) {
          indices[i] = i;
        }
        Comparator<Integer> comp = (Integer i, Integer j) -> Double.compare(E_2.get(i), E_2.get(j));
        Arrays.sort(indices, comp);
        int[] permutations = new int[indices.length];
        for (int i = 0; i < indices.length; i++) {
            permutations[i] = indices[i];
        }
        // free array
        indices = null;

        // Get result, a permuted A matrix based on the sorted E_2
        Matrix R = getPermutedMatrix(A, permutations);
        List<L> labels = getPermutedLabels(labeledMatrix.getLabels(), permutations);

        return LabeledDenseMatrix.of(R, labels);
    }

    /**
     * Get the row and column permutation of A based on <code>permutations</code>
     *
     * @param A
     *            The matrix to permute
     * @param permutations
     *            The permutations
     * @return The permuted matrix
     */
    public static Matrix getPermutedMatrix(Matrix A, int[] permutations) {
        // R = P * A * P'
        PermutationMatrix P = new PermutationMatrix(permutations);
        Matrix P_r = P.mult(A, new DenseMatrix(A.numRows(), A.numColumns()));
        Matrix P_c = P_r.transBmult(P, new DenseMatrix(A.numRows(), A.numColumns()));
        return P_c;
    }

    /**
     * Given an arbitrary type as labels and a permutation vector, this method returns the reordered
     * labels
     *
     * @param originalLabels
     *            The original labels in their original order
     * @param permutations
     *            The permutation vector
     * @return The reordered labels based on <code>permutations</code>
     */
    public static <L extends Serializable> List<L> getPermutedLabels(List<L> originalLabels,
                                                                     int[] permutations) {
        Preconditions.checkArgument(permutations.length == originalLabels.size(),
                                    "The permutation vector and labels must have the same length");
        List<L> result = Lists.newArrayListWithCapacity(originalLabels.size());
        for (int perm : permutations) {
            L label = originalLabels.get(perm);
            result.add(label);
        }
        return result;
    }

}
