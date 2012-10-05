package org.asoem.greyfish.core.genes;

import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 09:37
 */
@ClassGroup(tags = "traits")
public class QuantitativeTrait extends AbstractTrait<Double> {

    @Element
    private Callback<? super QuantitativeTrait, Double> initializationKernel;

    @Element
    private Callback<? super QuantitativeTrait, Double> mutationKernel;

    @Element
    private Callback<? super QuantitativeTrait, Double> segregationKernel;

    private Double value = 0.0;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public QuantitativeTrait() {
    }

    protected QuantitativeTrait(QuantitativeTrait doubleMutableGene, DeepCloner cloner) {
        super(doubleMutableGene, cloner);
        this.initializationKernel = doubleMutableGene.initializationKernel;
        this.mutationKernel = doubleMutableGene.mutationKernel;
        this.segregationKernel = doubleMutableGene.segregationKernel;
    }

    protected QuantitativeTrait(AbstractBuilder<? extends QuantitativeTrait, ? extends AbstractBuilder> builder) {
        super(builder);
        this.initializationKernel = builder.initializationKernel;
        this.mutationKernel = builder.mutationKernel;
        this.segregationKernel = builder.segregationKernel;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new QuantitativeTrait(this, cloner);
    }

    @Override
    public Class<Double> getAlleleClass() {
        return Double.class;
    }

    public Callback<? super QuantitativeTrait, Double> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super QuantitativeTrait, Double> getMutationKernel() {
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
        e.add("Initial Value", TypedValueModels.forField("initializationKernel", this, new TypeToken<Callback<? super QuantitativeTrait, Double>>() {}));
        e.add("Mutation(x)", TypedValueModels.forField("mutationKernel", this, new TypeToken<Callback<? super QuantitativeTrait, Double>>() {}));
        e.add("Recombination(x,y)", TypedValueModels.forField("segregationKernel", this, new TypeToken<Callback<? super QuantitativeTrait, Double>>() {}));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<QuantitativeTrait, Builder> {
        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected QuantitativeTrait checkedBuild() {
            return new QuantitativeTrait(this);
        }
    }

    protected static abstract class AbstractBuilder<C extends QuantitativeTrait, B extends AbstractBuilder<C, B>> extends AbstractAgentComponent.AbstractBuilder<C, B> {
        private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(AbstractBuilder.class);
        private static final Callback<Object,Double> DEFAULT_INITIALIZATION_KERNEL = Callbacks.constant(0.0);
        private static final Callback<Object,Double> DEFAULT_MUTATION_KERNEL = Callbacks.constant(0.0);
        private static final Callback<Object,Double> DEFAULT_SEGREGATION_KERNEL = Callbacks.constant(0.0);

        private Callback<? super QuantitativeTrait, Double> initializationKernel = DEFAULT_INITIALIZATION_KERNEL;
        private Callback<? super QuantitativeTrait, Double> mutationKernel = DEFAULT_MUTATION_KERNEL;
        private Callback<? super QuantitativeTrait, Double> segregationKernel = DEFAULT_SEGREGATION_KERNEL;

        public B initialization(Callback<? super QuantitativeTrait, Double> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public B mutation(Callback<? super QuantitativeTrait, Double> callback) {
            this.mutationKernel = checkNotNull(callback);
            return self();
        }

        public B segregation(Callback<? super QuantitativeTrait, Double> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            if (initializationKernel == DEFAULT_INITIALIZATION_KERNEL)
                LOGGER.warn("Builder uses default initialization kernel for {}: {}", name, DEFAULT_INITIALIZATION_KERNEL);
            if (mutationKernel == DEFAULT_MUTATION_KERNEL)
                LOGGER.warn("Builder uses default mutation kernel for {}: {}", name, DEFAULT_MUTATION_KERNEL);
            if (segregationKernel == DEFAULT_SEGREGATION_KERNEL)
                LOGGER.warn("Builder uses default segregation kernel for {}: {}", name, DEFAULT_SEGREGATION_KERNEL);
        }
    }
}
