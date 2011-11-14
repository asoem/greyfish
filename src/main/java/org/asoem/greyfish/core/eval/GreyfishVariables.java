package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;
import org.asoem.greyfish.core.inject.CoreInjectorHolder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 11.11.11
 * Time: 09:16
 */
public class GreyfishVariables {
    private final static GreyfishVariableAccessorFactory FACTORY = CoreInjectorHolder.coreInjector().getInstance(GreyfishVariableAccessorFactory.class);

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object $(String expression, AgentComponent ctx) {
        checkNotNull(ctx);
        return FACTORY.get(expression, ctx.getClass()).apply(ctx);
    }
}
