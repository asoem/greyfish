package org.asoem.greyfish.core.traits;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.SimulationContext;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A generic heritable trait which holds an arbitrary value of type {@code T}. Initialization, mutation and segregation
 * of this value are defined using {@link Callback}s.
 *
 * @param <A> the type of the enclosing {@link Agent}
 * @param <T> the type of the value of this trait
 */
public class HeritableTrait<A extends Agent<A, SimulationContext<?>>, T> extends AbstractTrait<A, T> implements AgentTrait<A, T> {

    private final TypeToken<T> typeToken;

    private final Callback<? super AgentTrait<A, T>, T> initializationKernel;

    private final Callback<? super AgentTrait<A, T>, T> mutationKernel;

    private final Callback<? super AgentTrait<A, T>, T> segregationKernel;

    @Nullable
    private T value;

    private HeritableTrait(final HeritableTrait<A, T> trait, final DeepCloner cloner) {
        super(trait, cloner);
        this.initializationKernel = checkNotNull(trait.initializationKernel);
        this.mutationKernel = checkNotNull(trait.mutationKernel);
        this.segregationKernel = checkNotNull(trait.segregationKernel);
        this.typeToken = checkNotNull(trait.typeToken);
        this.value = trait.value;
    }

    private HeritableTrait(final AbstractBuilder<A, ? extends AgentTrait<A, T>, ? extends AbstractBuilder<A, ?, ?, T>, T> builder) {
        super(builder);
        this.initializationKernel = checkNotNull(builder.initializationKernel);
        this.mutationKernel = checkNotNull(builder.mutationKernel);
        this.segregationKernel = checkNotNull(builder.segregationKernel);
        this.typeToken = checkNotNull(builder.typeToken);
        this.value = builder.value;
    }

    @Override
    public DeepCloneable deepClone(final DeepCloner cloner) {
        return new HeritableTrait<A, T>(this, cloner);
    }

    @Override
    public TypeToken<T> getValueType() {
        return typeToken;
    }

    public Callback<? super AgentTrait<A, T>, T> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super AgentTrait<A, T>, T> getMutationKernel() {
        return mutationKernel;
    }

    @Override
    public void set(final T value) {
        this.value = value;
    }

    @Override
    public T mutate(final T allele) {
        return mutationKernel.apply(this, ImmutableMap.of("x", allele));
    }

    @Override
    public T segregate(final T allele1, final T allele2) {
        return segregationKernel.apply(this, ImmutableMap.of("x", allele1, "y", allele2));
    }

    @Override
    public T createInitialValue() {
        return initializationKernel.apply(this, ImmutableMap.<String, Object>of());
    }

    @Nullable
    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean isHeritable() {
        return true;
    }

    public Callback<? super AgentTrait<A, T>, T> getSegregationKernel() {
        return segregationKernel;
    }

    public static <A extends Agent<A, SimulationContext<?>>, T> Builder<A, T> builder() {
        return new Builder<A, T>();
    }

    private Object writeReplace() {
        return new Builder<A, T>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static class Builder<A extends Agent<A, SimulationContext<?>>, T> extends AbstractBuilder<A, HeritableTrait<A, T>, Builder<A, T>, T> implements Serializable {
        private Builder() {
        }

        private Builder(final HeritableTrait<A, T> quantitativeTrait) {
            super(quantitativeTrait);
        }

        @Override
        protected Builder<A, T> self() {
            return this;
        }

        @Override
        protected HeritableTrait<A, T> checkedBuild() {
            return new HeritableTrait<A, T>(this);
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return build();
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed: " + e);
            }
        }

        private static final long serialVersionUID = 0;
    }

    protected abstract static class AbstractBuilder<A extends Agent<A, SimulationContext<?>>, C extends HeritableTrait<A, T>, B extends AbstractBuilder<A, C, B, T>, T> extends AbstractAgentComponent.AbstractBuilder<A, C, B> implements Serializable {

        private final Callback<Object, T> defaultInitializationKernel = Callbacks.willThrow(new UnsupportedOperationException());
        private final Callback<Object, T> defaultMutationKernel = Callbacks.willThrow(new UnsupportedOperationException());
        private final Callback<Object, T> defaultSegregationKernel = Callbacks.willThrow(new UnsupportedOperationException());

        private Callback<? super AgentTrait<A, T>, T> initializationKernel = defaultInitializationKernel;
        private Callback<? super AgentTrait<A, T>, T> mutationKernel = defaultMutationKernel;
        private Callback<? super AgentTrait<A, T>, T> segregationKernel = defaultSegregationKernel;
        @Nullable
        private T value;
        private TypeToken<T> typeToken;

        protected AbstractBuilder(final HeritableTrait<A, T> quantitativeTrait) {
            super(quantitativeTrait);
            this.initializationKernel = quantitativeTrait.initializationKernel;
            this.mutationKernel = quantitativeTrait.mutationKernel;
            this.segregationKernel = quantitativeTrait.segregationKernel;
            this.value = quantitativeTrait.value;
        }

        protected AbstractBuilder() {
        }

        public final B initialization(final Callback<? super AgentTrait<A, T>, T> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public final B mutation(final Callback<? super AgentTrait<A, T>, T> callback) {
            this.mutationKernel = checkNotNull(callback);
            return self();
        }

        public final B segregation(final Callback<? super AgentTrait<A, T>, T> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        public final B ofType(final TypeToken<T> typeToken) {
            this.typeToken = typeToken;
            return self();
        }

        // only used internally for serialization
        protected final B value(@Nullable final T value) {
            this.value = value;
            return self();
        }
    }
}
