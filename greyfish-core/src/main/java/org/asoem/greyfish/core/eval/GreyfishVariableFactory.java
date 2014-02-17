package org.asoem.greyfish.core.eval;

import com.google.inject.Inject;


public class GreyfishVariableFactory {

    private GreyfishVariableFactory() {}

    @Inject
    private static GreyfishVariableAccessorFactory FACTORY;

    public static Object $(final String expression, final Object ctx) {
        return FACTORY.get(expression, Object.class).apply(ctx);
    }

    public static Object dollar(final String expression, final Object ctx) {
        return $(expression, ctx);
    }
}
