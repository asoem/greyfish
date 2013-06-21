package org.asoem.greyfish.core.traits;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.*;

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 09:37
 */
@Tagged("traits")
public class HeritableTrait<A extends Agent<A, ?>, T> extends AbstractTrait<A, T> implements AgentTrait<A,T> {

    private final TypeToken<T> typeToken;

    private final Callback<? super AgentTrait<A, T>, T> initializationKernel;

    private final Callback<? super AgentTrait<A, T>, T> mutationKernel;

    private final Callback<? super AgentTrait<A, T>, T> segregationKernel;

    @Nullable
    private T value;

    private HeritableTrait(HeritableTrait<A, T> trait, DeepCloner cloner) {
        super(trait, cloner);
        this.initializationKernel = trait.initializationKernel;
        this.mutationKernel = trait.mutationKernel;
        this.segregationKernel = trait.segregationKernel;
        this.value = trait.value;
        this.typeToken = trait.typeToken;
    }

    private HeritableTrait(AbstractBuilder<A, ? extends AgentTrait<A, T>, ? extends AbstractBuilder<A, ?, ?, T>, T> builder) {
        super(builder);
        this.initializationKernel = builder.initializationKernel;
        this.mutationKernel = builder.mutationKernel;
        this.segregationKernel = builder.segregationKernel;
        this.value = builder.value;
        this.typeToken = builder.typeToken;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
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
    public void set(T value) {
        this.value = value;
    }

    @Override
    public T mutate(T allele) {
        return mutationKernel.apply(this, ImmutableMap.of("x", allele));
    }

    @Override
    public T segregate(T allele1, T allele2) {
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

    public static <A extends Agent<A, ?>, T> Builder<A, T> builder() {
        return new Builder<A, T>();
    }

    private Object writeReplace() {
        return new Builder<A, T>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException{
        throw new InvalidObjectException("Builder required");
    }

    public static class Builder<A extends Agent<A, ?>, T> extends AbstractBuilder<A, HeritableTrait<A, T>, Builder<A, T>, T> implements Serializable {
        private Builder() {}

        private Builder(HeritableTrait<A, T> quantitativeTrait) {
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

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends HeritableTrait<A, T>, B extends AbstractBuilder<A, C, B, T>, T> extends AbstractAgentComponent.AbstractBuilder<A, C, B> implements Serializable {

        private final Callback<Object, T> DEFAULT_INITIALIZATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private final Callback<Object, T> DEFAULT_MUTATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private final Callback<Object, T> DEFAULT_SEGREGATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());

        private Callback<? super AgentTrait<A, T>, T> initializationKernel = DEFAULT_INITIALIZATION_KERNEL;
        private Callback<? super AgentTrait<A, T>, T> mutationKernel = DEFAULT_MUTATION_KERNEL;
        private Callback<? super AgentTrait<A, T>, T> segregationKernel = DEFAULT_SEGREGATION_KERNEL;
        @Nullable
        private T value;
        private TypeToken<T> typeToken;

        protected AbstractBuilder(HeritableTrait<A, T> quantitativeTrait) {
            super(quantitativeTrait);
            this.initializationKernel = quantitativeTrait.initializationKernel;
            this.mutationKernel = quantitativeTrait.mutationKernel;
            this.segregationKernel = quantitativeTrait.segregationKernel;
            this.value = quantitativeTrait.value;
        }

        protected AbstractBuilder() {}

        public B initialization(Callback<? super AgentTrait<A, T>, T> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public B mutation(Callback<? super AgentTrait<A, T>, T> callback) {
            this.mutationKernel = checkNotNull(callback);
            return self();
        }

        public B segregation(Callback<? super AgentTrait<A, T>, T> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        public B ofType(TypeToken<T> typeToken) {
            this.typeToken = typeToken;
            return self();
        }

        // only used internally for serialization
        protected B value(@Nullable T value) {
            this.value = value;
            return self();
        }
    }
}
