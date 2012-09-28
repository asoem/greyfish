package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.properties.GFProperty;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 26.09.12
 * Time: 18:31
 */
public class Agents {
    public static Function<Agent, GFProperty<?>> propertyAccessor(final String propertyName) {
        return new Function<Agent, GFProperty<?>>() {
            private final Predicate<? super GFProperty<?>> random_sperm = new Predicate<GFProperty<?>>() {
                @Override
                public boolean apply(GFProperty<?> input) {
                    return propertyName.equals(input.getName());
                }
            };

            @Override
            public GFProperty<?> apply(Agent input) {
                return checkNotNull(input).findProperty(random_sperm);
            }
        };
    }

    public static <T> Function<Agent, GFProperty<T>> propertyAccessor(final String propertyName, final TypeToken<GFProperty<T>> typeToken) {
        return cast(propertyAccessor(propertyName), typeToken);
    }

    public static Function<Agent, GeneComponent<?>> traitAccessor(final String traitName) {
        return new Function<Agent, GeneComponent<?>>() {
            private final Predicate<? super GeneComponent<?>> traitPredicate = new Predicate<GeneComponent<?>>() {
                @Override
                public boolean apply(GeneComponent<?> input) {
                    return traitName.equals(input.getName());
                }
            };

            @Override
            public GeneComponent<?> apply(Agent input) {
                return checkNotNull(input).findTrait(traitPredicate);
            }
        };
    }

    public static <T extends GeneComponent<?>> Function<Agent,T> traitAccessor(final String traitName, final Class<T> geneComponentClass) {
        return cast(traitAccessor(traitName), geneComponentClass);
    }

    private static <T> Function<Agent, T> cast(final Function<Agent, ?> accessorFunction, final Class<T> clazz) {
        return new Function<Agent, T>() {
            @Override
            public T apply(@Nullable Agent input) {
                return clazz.cast(accessorFunction.apply(input));
            }
        };
    }

    private static <T> Function<Agent, T> cast(final Function<Agent, ?> accessorFunction, final TypeToken<T> typeToken) {
        return new Function<Agent, T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T apply(@Nullable Agent input) {
                final Object o = accessorFunction.apply(input);
                assert o != null;
                if (!typeToken.isAssignableFrom(o.getClass()))
                    throw new ClassCastException("Component cannot be cast to " + typeToken.getType());
                return (T) o;
            }
        };
    }
}
