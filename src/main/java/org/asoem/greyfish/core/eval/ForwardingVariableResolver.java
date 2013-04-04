package org.asoem.greyfish.core.eval;

import com.google.common.collect.ForwardingObject;

import javax.annotation.Nullable;
import javax.script.Bindings;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 17:06
 */
public abstract class ForwardingVariableResolver extends ForwardingObject implements VariableResolver {

    @Override
    protected abstract VariableResolver delegate();

    @Override
    public Object resolve(String varName) throws VariableResolutionException {
        return delegate().resolve(varName);
    }

    @Override
    public Bindings bindings() {
        return delegate().bindings();
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
    public void append(@Nullable VariableResolver next) {
        delegate().append(next);
    }
}
