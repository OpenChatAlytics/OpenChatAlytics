package com.chatalytics.core.similarity;

import com.google.common.base.Preconditions;

import java.util.Arrays;

/**
 * All the valid dimensions that can be created for getting similarities
 *
 * @author giannis
 */
public enum SimilarityDimension {

    USER("user"),
    ROOM("room"),
    ENTITY("entity");

    private String dimensionName;

    private SimilarityDimension(String dimensionName) {
        this.dimensionName = dimensionName;
    }

    public String getDimensionName() {
        return dimensionName;
    }

    @Override
    public String toString() {
        return dimensionName;
    }

    /**
     * Constructs a {@link SimilarityDimension} from a string
     *
     * @param dimensionName
     *            The string representation of a {@link SimilarityDimension}
     * @return A {@link SimilarityDimension}
     * @throws IllegalArgumentException
     *             if <code>dimensionName</code> a <code>SimilarityDimension</code> can't be
     *             determined
     * @throws NullPointerException if <code>dimensionName</code> is null
     */
    public static SimilarityDimension fromDimensionName(String dimensionName) {
        Preconditions.checkNotNull(dimensionName,
                                   "Can't construct a similarity dimension from a null value");

        for (SimilarityDimension simDim : SimilarityDimension.values()) {
            if (dimensionName.equals(simDim.getDimensionName())) {
                return simDim;
            }
        }

        String msg = String.format("Can't construct similarity dimension from %s. "
                                   + "Supported values are %s", dimensionName,
                                   Arrays.deepToString(SimilarityDimension.values()));
        throw new IllegalArgumentException(msg);
    }
}
