package org.asoem.greyfish.core.eval.impl;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.asoem.greyfish.core.eval.GreyfishVariableAccessorFactory;
import org.asoem.greyfish.core.eval.VariableResolutionException;

import java.util.Map;

/**
 * User: christoph
 * Date: 16.09.11
 * Time: 15:24
 */
public class CachedGreyfishVariableAccessorFactory implements GreyfishVariableAccessorFactory {

    private final GreyfishVariableAccessorFactory delegate;

    private final Map<ResolverCacheKey, Function<?, ?>> resolverMap = Maps.newHashMap();

    public CachedGreyfishVariableAccessorFactory(final GreyfishVariableAccessorFactory delegate) {
        this.delegate = delegate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Function<T, ?> get(final String varName, final Class<T> context) throws VariableResolutionException {
        final ResolverCacheKey key = new ResolverCacheKey(varName, context);
        if (resolverMap.containsKey(key)) {
            return (Function<T, ?>) resolverMap.get(key);
        }
        else {
            final Function<T, ?> fun = delegate.get(varName, context);
            resolverMap.put(key, fun);
            return fun;
        }
    }

    @Override
    public boolean canConvert(final String name, final Class<?> contextClass) {
        final ResolverCacheKey key = new ResolverCacheKey(name, contextClass);
        return resolverMap.containsKey(key) || delegate.canConvert(name, contextClass);
    }

    private static class ResolverCacheKey {
        private final String varName;
        private final Class<?> context;

        public ResolverCacheKey(final String varName, final Class<?> context) {
            this.varName = varName;
            this.context = context;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final ResolverCacheKey that = (ResolverCacheKey) o;

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
