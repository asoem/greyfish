package org.asoem.greyfish.core.eval;

import com.google.common.collect.ForwardingObject;

import javax.annotation.Nullable;
import javax.script.Bindings;


public abstract class ForwardingVariableResolver extends ForwardingObject implements VariableResolver {

    @Override
    protected abstract VariableResolver delegate();

    @Override
    public Object resolve(final String varName) throws VariableResolutionException {
        return delegate().resolve(varName);
    }

    @Override
    public Bindings bindings() {
        return delegate().bindings();
    }

    @Override
    public boolean canResolve(final String name) {
        return delegate().canResolve(name);
    }

    @Override
    public VariableResolver getNext() {
        return delegate().getNext();
    }

    @Override
    public void append(@Nullable final VariableResolver next) {
        delegate().append(next);
    }
}
