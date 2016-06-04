package com.chatalytics.web.constant;

import com.google.common.base.Preconditions;

import java.util.Arrays;

public enum ActiveMethod {

    ToTV("totv"),
    ToMV("tomv");

    private String method;

    private ActiveMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return method;
    }

    /**
     * Constructs an {@link ActiveMethod} from a string
     *
     * @param method
     *            The string representation of a {@link ActiveMethod}
     * @return A {@link ActiveMethod}
     * @throws IllegalArgumentException
     *             if <code>ActiveMethod</code> from a <code>method</code> can't be determined
     * @throws NullPointerException if <code>method</code> is null
     */
    public static ActiveMethod fromMethodName(String method) {
        Preconditions.checkNotNull(method, "Can't construct method from a null value");

        for (ActiveMethod m : ActiveMethod.values()) {
            if (method.equals(m.getMethod())) {
                return m;
            }
        }

        String msg = String.format("Can't construct method from %s. Supported values are %s",
                                   method, Arrays.deepToString(ActiveMethod.values()));
        throw new IllegalArgumentException(msg);
    }
}
