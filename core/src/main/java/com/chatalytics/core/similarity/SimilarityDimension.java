package com.chatalytics.core.similarity;

import java.util.Arrays;

/**
 * All the valid dimensions that can be created for getting similarities
 *
 * @author giannis
 */
public enum SimilarityDimension {
    USER("user"), ROOM("room"), ENTITY("entity");

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
     */
    public static SimilarityDimension fromDimensionName(String dimensionName) {
        if (USER.getDimensionName().equals(dimensionName)) {
            return USER;
        } else if (ENTITY.getDimensionName().equals(dimensionName)) {
            return ENTITY;
        } else if (ROOM.getDimensionName().equals(dimensionName)) {
            return ROOM;
        } else {
            String msg = String.format("Can't construct similarity dimension from %s. "
                                       + "Supported values are %s", dimensionName,
                                       Arrays.deepToString(SimilarityDimension.values()));
            throw new IllegalArgumentException(msg);
        }
    }
}
