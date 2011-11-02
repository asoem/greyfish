package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 21:57
 */
abstract class AbstractGreyfishVariableResolver<T extends AgentComponent> extends AbstractVariableResolver implements GreyfishVariableResolver<T> {

    @Nullable
    private T context;

    private final Class<T> contextClass;

    public AbstractGreyfishVariableResolver(Class<T> contextClass) {
        this.contextClass = checkNotNull(contextClass);
    }

    @Nullable
    @Override
    public final T getContext() {
        return context;
    }

    @Override
    public final void setContext(@Nullable T context) {
        this.context = context;
    }

    @Override
    public final Class<T> getContextClass() {
         return contextClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractGreyfishVariableResolver that = (AbstractGreyfishVariableResolver) o;

        return !(context != null ? !context.equals(that.context) : that.context != null) && contextClass.equals(that.contextClass);

    }

    @Override
    public int hashCode() {
        int result = context != null ? context.hashCode() : 0;
        result = 31 * result + contextClass.hashCode();
        return result;
    }
}
