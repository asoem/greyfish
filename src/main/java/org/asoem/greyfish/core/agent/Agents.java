package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.properties.AgentProperty;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 26.09.12
 * Time: 18:31
 */
public final class Agents {

    private Agents() {}

    public static <A extends Agent<A, ?>, T extends AgentProperty<A, ?>> ComponentAccessor<A, T> propertyAccessor(final String propertyName) {
        return new ComponentAccessor<A, T>() {
            private final Predicate<? super AgentProperty<A, ?>> random_sperm = new Predicate<AgentProperty<A, ?>>() {
                @Override
                public boolean apply(AgentProperty<A, ?> input) {
                    return propertyName.equals(input.getName());
                }
            };

            @Override
            public T apply(A input) {
                return (T) checkNotNull(input).findProperty(random_sperm);
            }
        };
    }

    public static <A extends Agent<A, ?>, T extends AgentTrait<A, ?>> ComponentAccessor<A, T> traitAccessor(final String traitName) {
        return new ComponentAccessor<A, T>() {
            private final Predicate<AgentTrait<A, ?>> traitPredicate = new Predicate<AgentTrait<A, ?>>() {
                @Override
                public boolean apply(AgentTrait<A, ?> input) {
                    return traitName.equals(input.getName());
                }
            };

            @Override
            public T apply(A input) {
                return (T) checkNotNull(input).findTrait(traitPredicate);
            }
        };
    }
}
