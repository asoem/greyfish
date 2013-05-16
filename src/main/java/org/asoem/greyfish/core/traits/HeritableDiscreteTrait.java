package org.asoem.greyfish.core.traits;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.*;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 09:37
 */
@Tagged("traits")
public class HeritableDiscreteTrait<A extends Agent<A, ?>, T> extends AbstractTrait<A, T> implements DiscreteTrait<A, T> {

    private final TypeToken<T> typeToken;

    private final Callback<? super DiscreteTrait<A, T>, T> initializationKernel;

    private final Callback<? super DiscreteTrait<A, T>, T> mutationKernel;

    private final Callback<? super DiscreteTrait<A, T>, T> segregationKernel;

    private T value;

    private final Set<T> states;

    private final Ordering<T> ordering;

    private HeritableDiscreteTrait(HeritableDiscreteTrait<A, T> trait, DeepCloner cloner) {
        super(trait, cloner);
        this.initializationKernel = trait.initializationKernel;
        this.mutationKernel = trait.mutationKernel;
        this.segregationKernel = trait.segregationKernel;
        this.value = trait.value;
        this.typeToken = trait.typeToken;
    }

    private HeritableDiscreteTrait(AbstractBuilder<A, ? extends DiscreteTrait<A, T>, ? extends AbstractBuilder<A, ?, ?, T>, T> builder) {
        super(builder);
        this.initializationKernel = builder.initializationKernel;
        this.mutationKernel = builder.mutationKernel;
        this.segregationKernel = builder.segregationKernel;
        this.value = builder.value;
        this.typeToken = builder.typeToken;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new HeritableDiscreteTrait<A, T>(this, cloner);
    }

    @Override
    public TypeToken<T> getValueType() {
        return typeToken;
    }

    public Callback<? super DiscreteTrait<A, T>, T> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super DiscreteTrait<A, T>, T> getMutationKernel() {
        return mutationKernel;
    }

    @Override
    public void set(T value) {
        checkArgument(getStates().contains(value));
        this.value = value;
    }

    @Override
    public T mutate(T allele) {
        checkArgument(getStates().contains(value));
        final T x = mutationKernel.apply(this, ImmutableMap.of("x", allele));
        if (getStates().contains(x))
            throw new AssertionError("Mutation callback returned an invalid value: " + x);
        return x;
    }

    @Override
    public T segregate(T allele1, T allele2) {
        return segregationKernel.apply(this, ImmutableMap.of("x", allele1, "y", allele2));
    }

    @Override
    public T createInitialValue() {
        return initializationKernel.apply(this, ImmutableMap.<String, Object>of());
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean isHeritable() {
        return true;
    }

    public Callback<? super DiscreteTrait<A, T>, T> getSegregationKernel() {
        return segregationKernel;
    }

    public static <A extends Agent<A, ?>, T extends Comparable<T>> Builder<A, T> builder() {
        return new Builder<A, T>();
    }

    private Object writeReplace() {
        return new Builder<A, T>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException{
        throw new InvalidObjectException("Builder required");
    }

    @Override
    public Set<T> getStates() {
        return states;
    }

    @Override
    public int size() {
        return getStates().size();
    }

    @Override
    public Ordering<T> getOrdering() {
        return ordering;
    }

    public static class Builder<A extends Agent<A, ?>, T> extends AbstractBuilder<A, HeritableDiscreteTrait<A, T>, Builder<A, T>, T> implements Serializable {
        private Builder() {}

        private Builder(HeritableDiscreteTrait<A, T> quantitativeTrait) {
            super(quantitativeTrait);
        }

        @Override
        protected Builder<A, T> self() {
            return this;
        }

        @Override
        protected HeritableDiscreteTrait<A, T> checkedBuild() {
            return new HeritableDiscreteTrait<A, T>(this);
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

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends HeritableDiscreteTrait<A, T>, B extends AbstractBuilder<A, C, B, T>, T> extends AbstractAgentComponent.AbstractBuilder<A, C, B> implements Serializable {

        private final Callback<Object, T> DEFAULT_INITIALIZATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private final Callback<Object, T> DEFAULT_MUTATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private final Callback<Object, T> DEFAULT_SEGREGATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());

        private Callback<? super DiscreteTrait<A, T>, T> initializationKernel = DEFAULT_INITIALIZATION_KERNEL;
        private Callback<? super DiscreteTrait<A, T>, T> mutationKernel = DEFAULT_MUTATION_KERNEL;
        private Callback<? super DiscreteTrait<A, T>, T> segregationKernel = DEFAULT_SEGREGATION_KERNEL;
        private T value;
        private TypeToken<T> typeToken;

        protected AbstractBuilder(HeritableDiscreteTrait<A, T> quantitativeTrait) {
            super(quantitativeTrait);
            this.initializationKernel = quantitativeTrait.initializationKernel;
            this.mutationKernel = quantitativeTrait.mutationKernel;
            this.segregationKernel = quantitativeTrait.segregationKernel;
            this.value = quantitativeTrait.value;
        }

        protected AbstractBuilder() {}

        public B initialization(Callback<? super DiscreteTrait<A, T>, T> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public B mutation(Callback<? super DiscreteTrait<A, T>, T> callback) {
            this.mutationKernel = checkNotNull(callback);
            return self();
        }

        public B segregation(Callback<? super DiscreteTrait<A, T>, T> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        // only used internally for serialization
        protected B value(T value) {
            this.value = value;
            return self();
        }
    }
}
