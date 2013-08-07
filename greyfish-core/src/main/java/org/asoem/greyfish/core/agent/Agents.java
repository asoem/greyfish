package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.traits.AgentTrait;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A collection of helper functions dealing with agents.
 */
@SuppressWarnings("UnusedDeclaration")
public final class Agents {

    private Agents() {
        throw new AssertionError();
    }

    /**
     * Create a fast accessor for properties named {@code propertyName} of agents.
     * (Fast means faster than {@link Agent#getProperty(String)})
     * @param propertyName the name of the property to find
     * @param <A> the {@link Agent}'s type
     * @param <T> the type of the property
     * @return a new accessor instance
     */
    public static <A extends Agent<A, ?>, T extends AgentProperty<A, ?>>
    ComponentAccessor<A, T> propertyAccessor(final String propertyName) {
        return new ComponentAccessor<A, T>() {
            private final Predicate<? super AgentProperty<A, ?>> componentNamePredicate =
                    new ComponentNamePredicate<A>(propertyName);

            @Override
            public T apply(final A input) {
                return (T) checkNotNull(input).findProperty(componentNamePredicate);
            }
        };
    }

    /**
     * Create a fast accessor for traits named {@code traitName} of agents.
     * (Fast means faster than {@link Agent#getTrait(String)}
     * @param traitName the name of the trait to find
     * @param <A> the {@link Agent}'s type
     * @param <T> the type of the trait
     * @return a new accessor instance
     * @deprecated Use {@link #traitAccessor(String, com.google.common.reflect.TypeToken)}
     * or {@link #traitAccessor(String, Class)} instead
     */
    @Deprecated
    public static <A extends Agent<A, ?>, T extends AgentTrait<A, ?>>
    ComponentAccessor<A, T> traitAccessor(final String traitName) {
        return new ComponentAccessor<A, T>() {
            private final Predicate<AgentComponent<A>> componentNamePredicate =
                    new ComponentNamePredicate<A>(traitName);

            @Override
            public T apply(final A input) {
                return (T) checkNotNull(input).findTrait(componentNamePredicate);
            }
        };
    }

    /**
     * Create a fast accessor for traits named {@code traitName} of agents.
     * (Fast means faster than {@link Agent#getTrait(String)}
     * @param traitName the name of the trait to find
     * @param tClass the expected class of the trait
     * @param <A> the {@link Agent}'s type
     * @param <T> the type of the trait
     * @return a new accessor instance
     * @throws ClassCastException if the trait could be found but is not of type {@code tClass}
     */
    public static <A extends Agent<A, ?>, T extends AgentTrait<A, ?>>
    ComponentAccessor<A, T> traitAccessor(final String traitName, final Class<T> tClass) {
        return new ComponentAccessor<A, T>() {
            private final Predicate<AgentComponent<A>> componentNamePredicate =
                    new ComponentNamePredicate<A>(traitName);

            @Override
            public T apply(final A input) {
                return tClass.cast(checkNotNull(input).findTrait(componentNamePredicate));
            }
        };
    }

    /**
     * Create a fast accessor for traits named {@code traitName} of agents.
     * (Fast means faster than {@link Agent#getTrait(String)}
     * @param traitName the name of the trait to find
     * @param typeToken the expected type of the trait value
     *      {@link org.asoem.greyfish.core.traits.AgentTrait#getValueType()}
     * @param <A> the {@link Agent}'s type
     * @param <V> the value type of the trait
     * @return a new accessor instance
     * @throws IllegalArgumentException if the trait could be found
     *      but its value type is not assignable to {@code typeToken}
     */
    public static <A extends Agent<A, ?>, V>
    ComponentAccessor<A, AgentTrait<A, V>> traitAccessor(final String traitName, final TypeToken<V> typeToken) {
        return new ComponentAccessor<A, AgentTrait<A, V>>() {
            private final Predicate<AgentComponent<A>> componentNamePredicate
                    = new ComponentNamePredicate<A>(traitName);

            // Safe cast if the value type of the trait is assignable from given typeToken
            @SuppressWarnings("unchecked")
            @Override
            public AgentTrait<A, V> apply(final A input) {
                final AgentTrait<A, ?> trait = checkNotNull(input).findTrait(componentNamePredicate);
                if (!typeToken.isAssignableFrom(trait.getValueType())) {
                    final String message = String.format("Value type %s is not assignable from type %s",
                            input, typeToken);
                    throw new IllegalArgumentException(message);
                }
                return (AgentTrait<A, V>) trait;
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
