package org.asoem.greyfish.core.eval;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 26.10.11
 * Time: 13:05
 */
public abstract class AbstractVariableResolver implements VariableResolver {

    private VariableResolver nextVariableResolver;

    @Override
    @Nullable
    public VariableResolver getNext() {
        return nextVariableResolver;
    }

    @Override
    public void setNext(@Nullable VariableResolver next) {
        this.nextVariableResolver = next;
    }

    @Override
    public final boolean canResolve(String name) {
        return canResolveLocal(name) || nextVariableResolver != null && nextVariableResolver.canResolve(name);
    }

    protected abstract boolean canResolveLocal(String name);

    @Override
    public final Object resolve(String varName) throws VariableResolutionException {
        if (canResolveLocal(varName))
            return resolveLocal(varName);

        if (nextVariableResolver != null)
            return nextVariableResolver.resolve(varName);

        throw new VariableResolutionException("Not match for variable " + varName);
    }

    @Nullable
    protected abstract Object resolveLocal(String varName);
}
