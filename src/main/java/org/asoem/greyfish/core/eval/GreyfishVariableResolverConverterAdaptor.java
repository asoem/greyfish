package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 14:40
 */
public class GreyfishVariableResolverConverterAdaptor<T extends AgentComponent> extends AbstractGreyfishVariableResolver<T> {

    private final ResolverConverter converter;
    private final Class<T> contextClass;

    public GreyfishVariableResolverConverterAdaptor(ResolverConverter converter, final Class<T> contextClass) {
        this.converter = converter;
        this.contextClass = contextClass;
    }

    @Override
    protected boolean canResolveLocal(String name) {
        return converter.canConvert(name, contextClass);
    }

    @Override
    protected Object resolveLocal(String varName) {
        return converter.get(varName, contextClass).apply(context);
    }
}
