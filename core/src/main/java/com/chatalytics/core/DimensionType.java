package com.chatalytics.core;

import com.google.common.base.Preconditions;

import java.util.Arrays;

/**
 * All the valid dimensions that can be created for various web requests
 *
 * @author giannis
 */
public enum DimensionType {

    USER("user"),
    ROOM("room"),
    ENTITY("entity"),
    EMOJI("emoji");

    private String dimensionName;

    private DimensionType(String dimensionName) {
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
     * Constructs a {@link DimensionType} from a string
     *
     * @param dimensionName
     *            The string representation of a {@link DimensionType}
     * @return A {@link DimensionType}
     * @throws IllegalArgumentException
     *             if <code>DimensionType</code> from a <code>dimensionName</code> can't be
     *             determined
     * @throws NullPointerException if <code>dimensionName</code> is null
     */
    public static DimensionType fromDimensionName(String dimensionName) {
        Preconditions.checkNotNull(dimensionName, "Can't construct dimension from a null value");

        for (DimensionType simDim : DimensionType.values()) {
            if (dimensionName.equals(simDim.getDimensionName())) {
                return simDim;
            }
        }

        String msg = String.format("Can't construct dimension from %s. Supported values are %s",
                                   dimensionName, Arrays.deepToString(DimensionType.values()));
        throw new IllegalArgumentException(msg);
    }
}
