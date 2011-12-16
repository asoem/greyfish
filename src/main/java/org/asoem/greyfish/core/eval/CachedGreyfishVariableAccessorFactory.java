package org.asoem.greyfish.core.eval;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.asoem.greyfish.core.individual.AgentComponent;

import java.util.Map;

/**
 * User: christoph
 * Date: 16.09.11
 * Time: 15:24
 */
public class CachedGreyfishVariableAccessorFactory implements GreyfishVariableAccessorFactory {

    private final GreyfishVariableAccessorFactory delegate;

    private final Map<ResolverCacheKey, Function<AgentComponent, ?>> resolverMap = Maps.newHashMap();

    public CachedGreyfishVariableAccessorFactory(GreyfishVariableAccessorFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public Function<AgentComponent, ?> get(String varName, Class<? extends AgentComponent> context) throws VariableResolutionException {
        ResolverCacheKey key = new ResolverCacheKey(varName, context);
        if (resolverMap.containsKey(key)) {
            return resolverMap.get(key);
        }
        else {
            Function<AgentComponent, ?> fun = delegate.get(varName, context);
            resolverMap.put(key, fun);
            return fun;
        }
    }

    @Override
    public boolean canConvert(String name, Class<? extends AgentComponent> contextClass) {
        ResolverCacheKey key = new ResolverCacheKey(name, contextClass);
        return resolverMap.containsKey(key) || delegate.canConvert(name, contextClass);
    }

    private static class ResolverCacheKey {
        private final String varName;
        private final Class<? extends AgentComponent> context;

        public ResolverCacheKey(String varName, Class<? extends AgentComponent> context) {
            this.varName = varName;
            this.context = context;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ResolverCacheKey that = (ResolverCacheKey) o;

            return !(context != null ? !context.equals(that.context) : that.context != null)
                    && !(varName != null ? !varName.equals(that.varName) : that.varName != null);

        }

        @Override
        public int hashCode() {
            int result = varName != null ? varName.hashCode() : 0;
            result = 31 * result + (context != null ? context.hashCode() : 0);
            return result;
        }
    }
}
