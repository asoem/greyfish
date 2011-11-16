package org.asoem.greyfish.core.eval;

import com.google.inject.Inject;
import org.asoem.greyfish.core.individual.AgentComponent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 11.11.11
 * Time: 09:16
 */
public class GreyfishVariableFactory {
    @Inject private static GreyfishVariableAccessorFactory FACTORY;

    @SuppressWarnings({"UnusedDeclaration"})
    public static Object $(String expression, AgentComponent ctx) {
        checkNotNull(ctx);
        return FACTORY.get(expression, ctx.getClass()).apply(ctx);
    }
}
