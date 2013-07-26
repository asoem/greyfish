package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;

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
            private final Predicate<? super AgentProperty<A, ?>> componentNamePredicate = new ComponentNamePredicate<A>(propertyName);

            @Override
            public T apply(final A input) {
                return (T) checkNotNull(input).findProperty(componentNamePredicate);
            }
        };
    }

    public static <A extends Agent<A, ?>, T extends AgentTrait<A, ?>> ComponentAccessor<A, T> traitAccessor(final String traitName) {
        return new ComponentAccessor<A, T>() {
            private final Predicate<AgentComponent<A>> componentNamePredicate = new ComponentNamePredicate<A>(traitName);

            @Override
            public T apply(final A input) {
                return (T) checkNotNull(input).findTrait(componentNamePredicate);
            }
        };
    }

    private static class ComponentNamePredicate<A extends Agent<A, ?>> implements Predicate<AgentComponent<A>> {
        private final String name;

        public ComponentNamePredicate(final String name) {
            this.name = checkNotNull(name);
        }

        @Override
        public boolean apply(final AgentComponent<A> input) {
            return name.equals(input.getName());
        }

        @Override
        public String toString() {
            return "Name of component == " + name;
        }

        @SuppressWarnings({"rawtypes", "RedundantIfStatement"})
        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof ComponentNamePredicate)) return false;

            final ComponentNamePredicate that = (ComponentNamePredicate) o;

            if (!name.equals(that.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }
}
