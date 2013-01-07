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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 09:37
 */
@Tagged("traits")
public class QuantitativeTrait<A extends Agent<A, ?>> extends AbstractTrait<A, Double> {

    @Element
    private Callback<? super QuantitativeTrait<A>, Double> initializationKernel;

    @Element
    private Callback<? super QuantitativeTrait<A>, Double> mutationKernel;

    @Element
    private Callback<? super QuantitativeTrait<A>, Double> segregationKernel;

    private Double value = 0.0;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private QuantitativeTrait() {}

    private QuantitativeTrait(QuantitativeTrait<A> doubleMutableGene, CloneMap cloner) {
        super(doubleMutableGene, cloner);
        this.initializationKernel = doubleMutableGene.initializationKernel;
        this.mutationKernel = doubleMutableGene.mutationKernel;
        this.segregationKernel = doubleMutableGene.segregationKernel;
        this.value = doubleMutableGene.value;
    }

    private QuantitativeTrait(AbstractBuilder<A, ? extends QuantitativeTrait<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
        this.initializationKernel = builder.initializationKernel;
        this.mutationKernel = builder.mutationKernel;
        this.segregationKernel = builder.segregationKernel;
        this.value = builder.value;
    }

    @Override
    public DeepCloneable deepClone(CloneMap cloneMap) {
        return new QuantitativeTrait<A>(this, cloneMap);
    }

    @Override
    public Class<Double> getAlleleClass() {
        return Double.class;
    }

    public Callback<? super QuantitativeTrait<A>, Double> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super QuantitativeTrait<A>, Double> getMutationKernel() {
        return mutationKernel;
    }

    @Override
    public void setAllele(Object allele) {
        checkArgument(allele instanceof Double);
        final Double newAllele = (Double) allele;
        checkArgument(!Double.isNaN(newAllele), "allele is NaN: " + allele);
        this.value = newAllele;
    }

    @Override
    public Double mutate(Double allele) {
        checkNotNull(allele);
        final Double x = mutationKernel.apply(this, ArgumentMap.of("x", allele));
        if (x == null || Double.isNaN(x))
            throw new AssertionError("Mutation callback returned an invalid value: " + x);
        return x;
    }

    @Override
    public Double segregate(Double allele1, Double allele2) {
        return segregationKernel.apply(this, ArgumentMap.of("x", allele1, "y", allele2));
    }

    @Override
    public Double createInitialValue() {
        return initializationKernel.apply(this, ArgumentMap.of());
    }

    @Override
    public Double getAllele() {
        return value;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Initial Value", TypedValueModels.forField("initializationKernel", this, new TypeToken<Callback<? super QuantitativeTrait<A>, Double>>() {}));
        e.add("Mutation(x)", TypedValueModels.forField("mutationKernel", this, new TypeToken<Callback<? super QuantitativeTrait<A>, Double>>() {}));
        e.add("Recombination(x,y)", TypedValueModels.forField("segregationKernel", this, new TypeToken<Callback<? super QuantitativeTrait<A>, Double>>() {}));
    }

    public Callback<? super QuantitativeTrait<A>, Double> getSegregationKernel() {
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

    public static class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, QuantitativeTrait<A>, Builder<A>> implements Serializable {
        private Builder() {}

        private Builder(QuantitativeTrait<A> quantitativeTrait) {
            super(quantitativeTrait);
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected QuantitativeTrait<A> checkedBuild() {
            return new QuantitativeTrait<A>(this);
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

    protected static abstract class AbstractBuilder<A extends Agent<A, ?>, C extends QuantitativeTrait<A>, B extends AbstractBuilder<A, C, B>> extends AbstractAgentComponent.AbstractBuilder<A, C, B> implements Serializable {

        private static final Callback<Object,Double> DEFAULT_INITIALIZATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private static final Callback<Object,Double> DEFAULT_MUTATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());
        private static final Callback<Object,Double> DEFAULT_SEGREGATION_KERNEL = Callbacks.willThrow(new UnsupportedOperationException());

        private Callback<? super QuantitativeTrait<A>, Double> initializationKernel = DEFAULT_INITIALIZATION_KERNEL;
        private Callback<? super QuantitativeTrait<A>, Double> mutationKernel = DEFAULT_MUTATION_KERNEL;
        private Callback<? super QuantitativeTrait<A>, Double> segregationKernel = DEFAULT_SEGREGATION_KERNEL;
        private double value;

        protected AbstractBuilder(QuantitativeTrait<A> quantitativeTrait) {
            super(quantitativeTrait);
            this.initializationKernel = quantitativeTrait.initializationKernel;
            this.mutationKernel = quantitativeTrait.mutationKernel;
            this.segregationKernel = quantitativeTrait.segregationKernel;
            this.value = quantitativeTrait.value;
        }

        protected AbstractBuilder() {}

        public B initialization(Callback<? super QuantitativeTrait<A>, Double> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public B mutation(Callback<? super QuantitativeTrait<A>, Double> callback) {
            this.mutationKernel = checkNotNull(callback);
            return self();
        }

        public B segregation(Callback<? super QuantitativeTrait<A>, Double> callback) {
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
