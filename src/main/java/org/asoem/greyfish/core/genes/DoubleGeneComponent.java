package org.asoem.greyfish.core.genes;

import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 09:37
 */
@ClassGroup(tags = "genes")
public class DoubleGeneComponent extends AbstractGeneComponent<Double> {

    @Element
    private Callback<? super DoubleGeneComponent, Double> initializationKernel;

    @Element
    private Callback<? super DoubleGeneComponent, Double> mutationKernel;

    @Element
    private Callback<? super DoubleGeneComponent, Double> segregationKernel;

    private Double value = 0.0;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public DoubleGeneComponent() {
    }

    protected DoubleGeneComponent(DoubleGeneComponent doubleMutableGene, DeepCloner cloner) {
        super(doubleMutableGene, cloner);
        this.initializationKernel = doubleMutableGene.initializationKernel;
        this.mutationKernel = doubleMutableGene.mutationKernel;
        this.segregationKernel = doubleMutableGene.segregationKernel;
    }

    protected DoubleGeneComponent(AbstractDoubleGeneBuilder<? extends DoubleGeneComponent, ? extends AbstractDoubleGeneBuilder> builder) {
        super(builder);
        this.initializationKernel = builder.initializationKernel;
        this.mutationKernel = builder.mutationKernel;
        this.segregationKernel = builder.segregationKernel;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DoubleGeneComponent(this, cloner);
    }

    @Override
    public Class<Double> getAlleleClass() {
        return Double.class;
    }

    public Callback<? super DoubleGeneComponent, Double> getInitializationKernel() {
        return initializationKernel;
    }

    public Callback<? super DoubleGeneComponent, Double> getMutationKernel() {
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
        e.add("Initial Value", TypedValueModels.forField("initializationKernel", this, new TypeToken<Callback<? super DoubleGeneComponent, Double>>() {}));
        e.add("Mutation(x)", TypedValueModels.forField("mutationKernel", this, new TypeToken<Callback<? super DoubleGeneComponent, Double>>() {}));
        e.add("Recombination(x,y)", TypedValueModels.forField("segregationKernel", this, new TypeToken<Callback<? super DoubleGeneComponent, Double>>() {}));
    }

    public static DoubleGeneBuilder builder() {
        return new DoubleGeneBuilder();
    }

    public static class DoubleGeneBuilder extends AbstractDoubleGeneBuilder<DoubleGeneComponent, DoubleGeneBuilder> {
        @Override
        protected DoubleGeneBuilder self() {
            return this;
        }

        @Override
        protected DoubleGeneComponent checkedBuild() {
            return new DoubleGeneComponent(this);
        }
    }

    protected static abstract class AbstractDoubleGeneBuilder<E extends DoubleGeneComponent, T extends AbstractDoubleGeneBuilder<E, T>> extends AbstractComponentBuilder<E, T> {
        private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDoubleGeneBuilder.class);
        private static final Callback<Object,Double> DEFAULT_INITIALIZATION_KERNEL = Callbacks.constant(0.0);
        private static final Callback<Object,Double> DEFAULT_MUTATION_KERNEL = Callbacks.constant(0.0);
        private static final Callback<Object,Double> DEFAULT_RECOMBINATION_KERNEL = Callbacks.constant(0.0);

        private Callback<? super DoubleGeneComponent, Double> initializationKernel = DEFAULT_INITIALIZATION_KERNEL;
        private Callback<? super DoubleGeneComponent, Double> mutationKernel = DEFAULT_MUTATION_KERNEL;
        private Callback<? super DoubleGeneComponent, Double> segregationKernel = DEFAULT_RECOMBINATION_KERNEL;

        public T initialization(Callback<? super DoubleGeneComponent, Double> callback) {
            this.initializationKernel = checkNotNull(callback);
            return self();
        }

        public T mutation(Callback<? super DoubleGeneComponent, Double> callback) {
            this.mutationKernel = checkNotNull(callback);
            return self();
        }

        public T segregation(Callback<? super DoubleGeneComponent, Double> callback) {
            this.segregationKernel = checkNotNull(callback);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            if (initializationKernel == DEFAULT_INITIALIZATION_KERNEL)
                LOGGER.warn("Using default initialization kernel: {}", DEFAULT_INITIALIZATION_KERNEL);
            if (mutationKernel == DEFAULT_MUTATION_KERNEL)
                LOGGER.warn("Using default mutation kernel: {}", DEFAULT_MUTATION_KERNEL);
            if (segregationKernel == DEFAULT_RECOMBINATION_KERNEL)
                LOGGER.warn("Using default segregation kernel: {}", DEFAULT_RECOMBINATION_KERNEL);
        }
    }
}
