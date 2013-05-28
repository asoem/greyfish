package org.asoem.greyfish.core.traits;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.*;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 09:37
 */
@Tagged("traits")
public class DoublePrecisionRealNumberTrait<A extends Agent<A, ?>> extends AbstractTrait<A, Double> implements AgentTrait<A,Double> {

    private static final TypeToken<Double> DOUBLE_TYPE_TOKEN = TypeToken.of(Double.class);

    private final Callback<? super AgentTrait<A, Double>, Double> initializationKernel;

    private final Callback<? super AgentTrait<A, Double>, Double> mutationKernel;

    private final Callback<? super AgentTrait<A, Double>, Double> segregationKernel;

    private double value = 0.0;

    private DoublePrecisionRealNumberTrait(DoublePrecisionRealNumberTrait<A> doubleMutableGene, DeepCloner cloner) {
        super(doubleMutableGene, cloner);
        this.initializationKernel = doubleMutableGene.initializationKernel;
        this.mutationKernel = doubleMutableGene.mutationKernel;
        this.segregationKernel = doubleMutableGene.segregationKernel;
        this.value = doubleMutableGene.value;
    }

    private DoublePrecisionRealNumberTrait(AbstractBuilder<A, ? extends AgentTrait<A, Double>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.initializationKernel = builder.initializationKernel;
        this.mutationKernel = builder.mutationKernel;
        this.segregationKernel = builder.segregationKernel;
        this.value = builder.value;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DoublePrecisionRealNumberTrait<A>(this, cloner);
    }

    @Override
    public TypeToken<Double> getValueType() {
        return DOUBLE_TYPE_TOKEN;
    }

    public Callback<? super AgentTrait<A, Double>, Double> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super AgentTrait<A, Double>, Double> getMutationKernel() {
        return mutationKernel;
    }

    @Override
    public void set(Double value) {
        checkNotNull(value);
        checkArgument(!Double.isNaN(value), "allele is NaN: " + value);
        this.value = value;
    }

    @Override
    public Double mutate(Double allele) {
        checkNotNull(allele);
        final Double x = mutationKernel.apply(this, ImmutableMap.of("x", allele));
        if (x == null || Double.isNaN(x))
            throw new AssertionError("Mutation callback returned an invalid value: " + x);
        return x;
    }

    @Override
    public Double segregate(Double allele1, Double allele2) {
        return segregationKernel.apply(this, ImmutableMap.of("x", allele1, "y", allele2));
    }

    @Override
    public Double createInitialValue() {
        return initializationKernel.apply(this, ImmutableMap.<String, Object>of());
    }

    @Override
    public Double get() {
        return value;
    }

    @Override
    public boolean isHeritable() {
        return true;
    }

    public Callback<? super AgentTrait<A, Double>, Double> getSegregationKernel() {
        return segregationKernel;
    }

    public static <A extends Agent<A, ?>> Builder<A> builder() {
        return new Builder<A>();
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException{
        throw new InvalidObjectException("Builder required");
    }

    public static class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, DoublePrecisionRealNumberTrait<A>, Builder<A>> implements Serializable {
        private Builder() {}

        private Builder(DoublePrecisionRealNumberTrait<A> quantitativeTrait) {
            super(quantitativeTrait);
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected DoublePrecisionRealNumberTrait<A> checkedBuild() {
            return new DoublePrecisionRealNumberTrait<A>(this);
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

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends DoublePrecisionRealNumberTrait<A>, B extends AbstractBuilder<A, C, B>> extends AbstractAgentComponent.AbstractBuilder<A, C, B> implements Serializable {

        private static final Callback<Object,Double> DEFAULT_INITIALIZATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private static final Callback<Object,Double> DEFAULT_MUTATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private static final Callback<Object,Double> DEFAULT_SEGREGATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());

        private Callback<? super AgentTrait<A, Double>, Double> initializationKernel = DEFAULT_INITIALIZATION_KERNEL;
        private Callback<? super AgentTrait<A, Double>, Double> mutationKernel = DEFAULT_MUTATION_KERNEL;
        private Callback<? super AgentTrait<A, Double>, Double> segregationKernel = DEFAULT_SEGREGATION_KERNEL;
        private double value;

        protected AbstractBuilder(DoublePrecisionRealNumberTrait<A> quantitativeTrait) {
            super(quantitativeTrait);
            this.initializationKernel = quantitativeTrait.initializationKernel;
            this.mutationKernel = quantitativeTrait.mutationKernel;
            this.segregationKernel = quantitativeTrait.segregationKernel;
            this.value = quantitativeTrait.value;
        }

        protected AbstractBuilder() {}

        public B initialization(Callback<? super AgentTrait<A, Double>, Double> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public B mutation(Callback<? super AgentTrait<A, Double>, Double> callback) {
            this.mutationKernel = checkNotNull(callback);
            return self();
        }

        public B segregation(Callback<? super AgentTrait<A, Double>, Double> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        // only used internally for serialization
        protected B value(double value) {
            this.value = value;
            return self();
        }
    }
}
