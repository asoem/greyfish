package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.core.individual.AgentComponent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 14:40
 */
public class GreyfishVariableResolverConverterAdaptor<T extends AgentComponent> extends AbstractGreyfishVariableResolver<T> {

    private final ResolverConverter converter;

    public GreyfishVariableResolverConverterAdaptor(ResolverConverter converter, final Class<T> contextClass) {
        super(contextClass);
        this.converter = checkNotNull(converter);
    }

    @Override
    protected boolean canResolveLocal(String name) {
        return converter.canConvert(name, getContextClass());
    }

    @Override
    protected Object resolveLocal(String varName) {
        return converter.get(varName, getContextClass()).apply(getContext());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GreyfishVariableResolverConverterAdaptor that = (GreyfishVariableResolverConverterAdaptor) o;

        return converter.equals(that.converter);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + converter.hashCode();
        return result;
    }
}
