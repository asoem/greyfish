package org.asoem.greyfish.core.eval;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 17:06
 */
public abstract class ForwardingVariableResolver implements VariableResolver {

    public abstract VariableResolver delegate();

    @Override
    public Object resolve(String varName) throws VariableResolutionException {
        return delegate().resolve(varName);
    }

    @Override
    public boolean canResolve(String name) {
        return delegate().canResolve(name);
    }

    @Override
    public VariableResolver getNext() {
        return delegate().getNext();
    }

    @Override
    public void setNext(@Nullable VariableResolver next) {
        delegate().setNext(next);
    }
}
