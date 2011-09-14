package org.asoem.greyfish.core.eval;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.asoem.greyfish.core.individual.GFComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 14:40
 */
public class VariableResolverFactory implements Function<Class<? extends GFComponent>, VariableResolver> {

    private final Map<ResolverCacheKey, Function<GFComponent, Object>> resolverMap = Maps.newHashMap();
    private final ResolverConverter converter;

    public VariableResolverFactory(ResolverConverter converter) {
        this.converter = converter;
    }

    @Override
    public VariableResolver apply(@Nullable final Class<? extends GFComponent> contextClass) {
        return new AbstractVariableResolver() {
            @Override
            public Object resolve(@Nonnull String varName) {
                return get(varName, contextClass).apply(getContext());
            }
        };
    }

    public VariableResolver createForContext(Class<? extends GFComponent> context) {
        return apply(context);
    }

    private synchronized Function<GFComponent, Object> get(String varName, Class<? extends GFComponent> context) {
        ResolverCacheKey key = new ResolverCacheKey(varName, context);
        if (resolverMap.containsKey(key)) {
            return resolverMap.get(key);
        }
        else {
            Function<GFComponent, Object> fun = converter.get(varName, context); // TODO: handle error / null
            resolverMap.put(key, fun);
            return fun;
        }
    }

    private static class ResolverCacheKey {
        private final String varName;
        private final Class<? extends GFComponent> context;

        public ResolverCacheKey(String varName, Class<? extends GFComponent> context) {
            this.varName = varName;
            this.context = context;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ResolverCacheKey that = (ResolverCacheKey) o;

            if (context != null ? !context.equals(that.context) : that.context != null) return false;
            if (varName != null ? !varName.equals(that.varName) : that.varName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = varName != null ? varName.hashCode() : 0;
            result = 31 * result + (context != null ? context.hashCode() : 0);
            return result;
        }
    }
}
