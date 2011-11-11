package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 11.11.11
 * Time: 09:16
 */
public enum SingletonGreyfishVariableSelector implements GreyfishVariableSelector {
    INSTANCE;

    private GreyfishVariableAccessorFactory greyfishVariableAccessorFactory = new CachedGreyfishVariableAccessorFactory(SingletonGreyfishGreyfishVariableAccessorFactory.INSTANCE);

    public GreyfishVariableAccessorFactory getGreyfishVariableAccessorFactory() {
        return greyfishVariableAccessorFactory;
    }

    public void setGreyfishVariableAccessorFactory(GreyfishVariableAccessorFactory greyfishVariableAccessorFactory) {
        this.greyfishVariableAccessorFactory = checkNotNull(greyfishVariableAccessorFactory);
    }

    @Override
    public Object $(String expression, AgentComponent ctx) {
        checkNotNull(ctx);
        return greyfishVariableAccessorFactory.get(expression, ctx.getClass()).apply(ctx);
    }

    public static Object apply(String expression, AgentComponent ctx) {
        return INSTANCE.$(expression, ctx);
    }
}
