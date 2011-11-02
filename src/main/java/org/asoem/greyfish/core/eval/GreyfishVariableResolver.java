package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 18.05.11
 * Time: 17:00
 */
public interface GreyfishVariableResolver<T extends AgentComponent> extends VariableResolver {
    /**
     *
     * @return the {@code AgentComponent} which serves as the context for this {@code GreyfishVariableResolver}.
     * The context will be used to resolve variable references made in an {@link GreyfishExpression} which are contextual (Commonly prefixed with 'this.')
     */
    @Nullable
    T getContext();

    /**
     * Set the context object for this {@code VariableResolver}.
     * The context will be used to resolve variable references made in an {@link GreyfishExpression} which are contextual (Commonly prefixed with 'this.')
     * @param context the context object for this {@code GreyfishVariableResolver}.
     */
    void setContext(@Nullable T context);

    /**
     * @return The {@code Class} of the context for this {@code GreyfishVariableResolver}
     */
    Class<T> getContextClass();
}
