package com.chatalytics.web.utils;

import com.google.common.base.Optional;

import org.apache.storm.shade.com.google.common.collect.ImmutableList;

import java.util.List;

import javax.annotation.Nullable;

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

    /**
     * Checks to see if the passed in list is null. If it is it creates an empty one. Note that the
     * empty list it returns is a singleton immutable list
     *
     * @param list
     *            The list to check
     * @return an empty {@link ImmutableList} if <code>list</code> is null, or list otherwise
     */
    public static <T> List<T> getListFromNullable(@Nullable List<T> list) {
        if (list == null) {
            return ImmutableList.of();
        } else {
            return list;
        }
    }

}
