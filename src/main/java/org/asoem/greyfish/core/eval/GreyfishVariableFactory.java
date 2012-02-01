package org.asoem.greyfish.core.eval;

import com.google.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 11.11.11
 * Time: 09:16
 */
public class GreyfishVariableFactory {
    @Inject private static GreyfishVariableAccessorFactory FACTORY;

    @SuppressWarnings({"UnusedDeclaration"})
    public static <T> Object $(String expression, T ctx) {
        checkNotNull(ctx);
        return FACTORY.get(expression, (Class<T>) ctx.getClass()).apply(ctx);
    }
}
