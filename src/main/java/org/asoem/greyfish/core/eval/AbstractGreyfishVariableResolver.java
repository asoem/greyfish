package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 21:57
 */
abstract class AbstractGreyfishVariableResolver<T extends AgentComponent> implements GreyfishVariableResolver<T> {

    protected T context;

    @Override
    public T getContext() {
        return context;
    }

    @Override
    public void setContext(T context) {
        this.context = checkNotNull(context);
    }
}
