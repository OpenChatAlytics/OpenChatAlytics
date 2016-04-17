package com.chatalytics.web.utils;

import com.google.common.base.Optional;

/**
 * Contains utility methods commonly used by resources
 *
 * @author giannis
 *
 */
public class ResourceUtils {

    /**
     * Helper method that returns an {@link Optional} with the value set if the parameter is not
     * null or non-empty.
     *
     * @param parameterStr
     *            The parameter to create the {@link Optional} for.
     * @return An {@link Optional} with the value set or absent appropriately.
     */
    public static Optional<String> getOptionalForParameter(String parameterStr) {
        if (parameterStr == null || parameterStr.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.of(parameterStr);
        }
    }

    /**
     * Helper method that returns an {@link Optional} with the value set if the parameter is not
     * null or non-empty.
     *
     * @param parameterStr
     *            The parameter to create the {@link Optional} for.
     * @return An {@link Optional} with the value set or absent appropriately.
     */
    public static Optional<Integer> getOptionalForParameterAsInt(String parameterStr) {
        if (parameterStr == null || parameterStr.isEmpty()) {
            return Optional.absent();
        } else {
            return Optional.of(Integer.parseInt(parameterStr));
        }
    }

}
