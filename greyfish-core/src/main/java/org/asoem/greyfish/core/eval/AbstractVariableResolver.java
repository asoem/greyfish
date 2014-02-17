package org.asoem.greyfish.core.eval;

import com.google.common.base.Objects;

import javax.annotation.Nullable;


public abstract class AbstractVariableResolver implements VariableResolver {

    private VariableResolver nextVariableResolver;

    @Override
    @Nullable
    public VariableResolver getNext() {
        return nextVariableResolver;
    }

    @Override
    public void append(@Nullable final VariableResolver next) {
        if (nextVariableResolver == null) {
            this.nextVariableResolver = next;
        } else {
            nextVariableResolver.append(next);
        }
    }

    @Override
    public final boolean canResolve(final String name) {
        return canResolveLocal(name) || nextVariableResolver != null && nextVariableResolver.canResolve(name);
    }

    protected abstract boolean canResolveLocal(String name);

    @Override
    public final Object resolve(final String varName) throws VariableResolutionException {
        if (canResolveLocal(varName)) {
            return resolveLocal(varName);
        }

        if (nextVariableResolver != null) {
            return nextVariableResolver.resolve(varName);
        }

        throw new VariableResolutionException("No match for variable " + varName);
    }

    @Nullable
    protected abstract Object resolveLocal(String varName);

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractVariableResolver that = (AbstractVariableResolver) o;

        if (!Objects.equal(bindings(), that.bindings())) {
            return false;
        }
        if (nextVariableResolver != null ? !nextVariableResolver.equals(that.nextVariableResolver) : that.nextVariableResolver != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = nextVariableResolver != null ? nextVariableResolver.hashCode() : 0;
        result = 31 * result + (bindings() != null ? bindings().hashCode() : 0);
        return result;
    }
}
