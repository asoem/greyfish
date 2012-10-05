package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 26.09.12
 * Time: 18:31
 */
public class Agents {
    public static ComponentAccessor<AgentProperty<?>> propertyAccessor(final String propertyName) {
        return new ComponentAccessor<AgentProperty<?>>() {
            private final Predicate<? super AgentProperty<?>> random_sperm = new Predicate<AgentProperty<?>>() {
                @Override
                public boolean apply(AgentProperty<?> input) {
                    return propertyName.equals(input.getName());
                }
            };

            @Override
            public AgentProperty<?> apply(Agent input) {
                return checkNotNull(input).findProperty(random_sperm);
            }
        };
    }

    public static <T> ComponentAccessor<AgentProperty<T>> propertyAccessor(final String propertyName, final Class<AgentProperty<T>> clazz) {
        return cast(propertyAccessor(propertyName), clazz);
    }

    public static ComponentAccessor<AgentTrait<?>> traitAccessor(final String traitName) {
        return new ComponentAccessor<AgentTrait<?>>() {
            private final Predicate<? super AgentTrait<?>> traitPredicate = new Predicate<AgentTrait<?>>() {
                @Override
                public boolean apply(AgentTrait<?> input) {
                    return traitName.equals(input.getName());
                }
            };

            @Override
            public AgentTrait<?> apply(Agent input) {
                return checkNotNull(input).findTrait(traitPredicate);
            }
        };
    }

    public static <T extends AgentTrait<?>> ComponentAccessor<T> traitAccessor(final String traitName, final Class<T> geneComponentClass) {
        return cast(traitAccessor(traitName), geneComponentClass);
    }

    private static <T extends AgentComponent> ComponentAccessor<T> cast(final ComponentAccessor<?> accessorFunction, final Class<T> clazz) {
        return new ComponentAccessor<T>() {
            @Override
            public T apply(@Nullable Agent input) {
                return clazz.cast(accessorFunction.apply(input));
            }
        };
    }
}
