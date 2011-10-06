package org.asoem.greyfish.core.eval;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 20.04.11
 * Time: 14:22
 */
public class VariableResolvers {
    static VariableResolver concat(final VariableResolver ... resolvers) {
        checkNotNull(resolvers);

        return new AbstractGreyfishVariableResolver() {
            @Override
            public boolean canResolve(final String name) {
                return Iterables.any(Arrays.asList(resolvers), new Predicate<VariableResolver>() {
                    @Override
                    public boolean apply(@Nullable VariableResolver o) {
                        return o != null && o.canResolve(name);
                    }
                });
            }

            @Override
            public Object resolve(@Nonnull  String s) {
                for (VariableResolver resolver : resolvers) {
                    if (resolver == null)
                        continue;
                    Object ret = resolver.resolve(s);
                    if (ret != null)
                        return ret;
                }
                return null;
            }
        };
    }

    static VariableResolver concat(final VariableResolver resolver1, final VariableResolver resolver2) {
        return new AbstractGreyfishVariableResolver() {
            @Override
            public boolean canResolve(String name) {
                return resolver1 != null && resolver1.canResolve(name)
                        || resolver2 != null && resolver2.canResolve(name);
            }

            @Override
            public Object resolve(@Nonnull String s) {
                Object ret = resolver1 == null ? null : resolver1.resolve(s);
                if (ret == null)
                    ret = resolver2 == null ? null : resolver2.resolve(s);
                return ret;
            }
        };
    }
}
