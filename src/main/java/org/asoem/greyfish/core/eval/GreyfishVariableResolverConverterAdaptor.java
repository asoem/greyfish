package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.GFComponent;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 14:40
 */
public class GreyfishVariableResolverConverterAdaptor<T extends GFComponent> extends AbstractGreyfishVariableResolver<T> {

    private final ResolverConverter converter;
    private final Class<T> contextClass;

    public GreyfishVariableResolverConverterAdaptor(ResolverConverter converter, final Class<T> contextClass) {
        this.converter = converter;
        this.contextClass = contextClass;
    }

    @Override
    public boolean canResolve(String name) {
        return converter.canConvert(name, contextClass);
    }

    @Override
    public Object resolve(String varName) throws VariableResolutionException {
        return converter.get(varName, contextClass).apply(context);
    }
}
