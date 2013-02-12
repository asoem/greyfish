package org.asoem.greyfish.core.genes;

import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.simpleframework.xml.Element;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 11:28
 */
@Tagged("traits")
public class RawTrait<A extends Agent<A, ?>, T> extends AbstractTrait<A, T> implements Serializable {

    @Element
    private Callback<? super RawTrait<A, T>, T> initializationKernel;

    @Element
    private Callback<? super RawTrait<A, T>, T> mutationKernel;

    @Element
    private Callback<? super RawTrait<A, T>, T> segregationKernel;

    private T value;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private RawTrait() {}

    private RawTrait(RawTrait<A, T> doubleMutableGene, DeepCloner cloner) {
        super(doubleMutableGene, cloner);
        this.initializationKernel = doubleMutableGene.initializationKernel;
        this.mutationKernel = doubleMutableGene.mutationKernel;
        this.segregationKernel = doubleMutableGene.segregationKernel;
        this.value = doubleMutableGene.value;
    }

    private RawTrait(AbstractBuilder<A, T, ? extends RawTrait<A, T>, ? extends AbstractBuilder<A, T, ?, ?>> builder) {
        super(builder);
        this.initializationKernel = builder.initializationKernel;
        this.mutationKernel = builder.mutationKernel;
        this.segregationKernel = builder.segregationKernel;
        this.value = builder.value;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new RawTrait<A, T>(this, cloner);
    }

    @Override
    public Class<? super T> getValueClass() {
        return (Class<? super T>) value.getClass();
    }

    public Callback<? super RawTrait<A, T>, T> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super RawTrait<A, T>, T> getMutationKernel() {
        return mutationKernel;
    }

    @Override
    public void setAllele(Object allele) {
        final T newAllele = (T) allele;
        this.value = newAllele;
    }

    @Override
    public T mutate(T allele) {
        checkNotNull(allele);
        final T x = mutationKernel.apply(this, ArgumentMap.of("x", allele));
        if (x == null)
            throw new AssertionError("Mutation callback returned an invalid value: " + x);
        return x;
    }

    @Override
    public T segregate(T allele1, T allele2) {
        return segregationKernel.apply(this, ArgumentMap.of("x", allele1, "y", allele2));
    }

    @Override
    public T createInitialValue() {
        return initializationKernel.apply(this, ArgumentMap.of());
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Initial Value", TypedValueModels.forField("initializationKernel", this, new TypeToken<Callback<? super QuantitativeTrait<A>, Double>>() {}));
        e.add("Mutation(x)", TypedValueModels.forField("mutationKernel", this, new TypeToken<Callback<? super QuantitativeTrait<A>, Double>>() {}));
        e.add("Recombination(x,y)", TypedValueModels.forField("segregationKernel", this, new TypeToken<Callback<? super QuantitativeTrait<A>, Double>>() {}));
    }

    public Callback<? super RawTrait<A, T>, T> getSegregationKernel() {
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

    public static class Builder<A extends Agent<A, ?>, T> extends AbstractBuilder<A, T, RawTrait<A, T>, Builder<A, T>> implements Serializable {
        private Builder() {}

        private Builder(RawTrait<A, T> quantitativeTrait) {
            super(quantitativeTrait);
        }

        @Override
        protected Builder<A, T> self() {
            return this;
        }

        @Override
        protected RawTrait<A, T> checkedBuild() {
            return new RawTrait<A, T>(this);
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

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, T, C extends RawTrait<A, T>, B extends AbstractBuilder<A, T, C, B>> extends AbstractAgentComponent.AbstractBuilder<A, C, B> implements Serializable {

        private static final Callback<Object,Object> DEFAULT_INITIALIZATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private static final Callback<Object,Object> DEFAULT_MUTATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private static final Callback<Object,Object> DEFAULT_SEGREGATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());

        private Callback<? super RawTrait<A, T>, T> initializationKernel = (Callback<? super RawTrait<A, T>, T>) DEFAULT_INITIALIZATION_KERNEL;
        private Callback<? super RawTrait<A, T>, T> mutationKernel = (Callback<? super RawTrait<A, T>, T>) DEFAULT_MUTATION_KERNEL;
        private Callback<? super RawTrait<A, T>, T> segregationKernel = (Callback<? super RawTrait<A, T>, T>) DEFAULT_SEGREGATION_KERNEL;
        private T value;

        protected AbstractBuilder(RawTrait<A, T> quantitativeTrait) {
            super(quantitativeTrait);
            this.initializationKernel = quantitativeTrait.initializationKernel;
            this.mutationKernel = quantitativeTrait.mutationKernel;
            this.segregationKernel = quantitativeTrait.segregationKernel;
            this.value = quantitativeTrait.value;
        }

        protected AbstractBuilder() {}

        public B initialization(Callback<? super RawTrait<A, T>, T> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public B mutation(Callback<? super RawTrait<A, T>, T> callback) {
            this.mutationKernel = checkNotNull(callback);
            return self();
        }

        public B segregation(Callback<? super RawTrait<A, T>, T> callback) {
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