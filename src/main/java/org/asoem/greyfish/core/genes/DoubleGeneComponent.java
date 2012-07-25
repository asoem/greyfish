package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.base.Callbacks.call;

/**
 * User: christoph
 * Date: 22.09.11
 * Time: 09:37
 */
@ClassGroup(tags = "genes")
public class DoubleGeneComponent extends AbstractGeneComponent<Double> {

    @Element
    private Callback<? super DoubleGeneComponent, Double> initialValue;

    @Element
    private Callback<? super DoubleGeneComponent, Double> mutation;

    @Element
    private Callback<? super DoubleGeneComponent, ? extends Product2<Double, Double>> recombination;

    private final GeneController<Double> geneController = new GeneController<Double>() {

        @Override
        public Double mutate(Object original) {
            return mutation.apply(DoubleGeneComponent.this, ArgumentMap.of("original", original));
        }

        @Override
        public Product2<Double, Double> recombine(Object first, Object second) {
            return recombination.apply(DoubleGeneComponent.this, ArgumentMap.of("first", first, "second", second));
        }

        @Override
        public double normalizedDistance(Double orig, Double copy) {
            return 1.0;
        }

        @Override
        public double normalizedWeightedDistance(Double orig, Double copy) {
            return normalizedDistance(orig, copy);
        }

        @Override
        public Double createInitialValue() {
            return call(initialValue, DoubleGeneComponent.this);
        }
    };

    private Double value = 0.0;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public DoubleGeneComponent() {
    }

    protected DoubleGeneComponent(DoubleGeneComponent doubleMutableGene, DeepCloner cloner) {
        super(doubleMutableGene, cloner);
        this.initialValue = doubleMutableGene.initialValue;
        this.mutation = doubleMutableGene.mutation;
        this.recombination = doubleMutableGene.recombination;
    }

    protected DoubleGeneComponent(AbstractDoubleGeneBuilder<? extends DoubleGeneComponent, ? extends AbstractDoubleGeneBuilder> builder) {
        super(builder);
        this.initialValue = builder.initialValue;
        this.mutation = builder.mutation;
        this.recombination = builder.recombination;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DoubleGeneComponent(this, cloner);
    }

    @Override
    public Class<Double> getSupplierClass() {
        return Double.class;
    }

    @Override
    public GeneController<Double> getGeneController() {
        return geneController;
    }

    public Callback<? super DoubleGeneComponent, Double> getInitialValue() {
        return initialValue;
    }

    public Callback<? super DoubleGeneComponent, Double> getMutation() {
        return mutation;
    }

    @Override
    public void setAllele(Object allele) {
        checkArgument(allele instanceof Double);
        this.value = (Double) allele;
    }

    @Override
    public Double getAllele() {
        return value;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        /*
        e.add("Initial Value", new AbstractTypedValueModel<GreyfishExpression>() {

            @Override
            protected void set(GreyfishExpression arg0) {
                initialValue = arg0;
            }

            @Override
            public GreyfishExpression get() {
                return initialValue;
            }
        });

        e.add("Mutation", new AbstractTypedValueModel<GreyfishExpression>() {
            @Override
            protected void set(GreyfishExpression arg0) {
                mutation = arg0;
            }

            @Override
            public GreyfishExpression get() {
                return mutation;
            }
        });
    */
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
        private static final Callback<Object,Double> DEFAULT_INITIAL_VALUE_CALLBACK = Callbacks.constant(0.0);
        private static final Callback<Object,Double> DEFAULT_MUTATION_CALLBACK = Callbacks.constant(0.0);
        private static final Callback<Object,Tuple2<Double,Double>> DEFAULT_RECOMBINATION_CALLBACK = Callbacks.constant(Tuple2.of(0.0, 0.0));

        private Callback<? super DoubleGeneComponent, Double> initialValue = DEFAULT_INITIAL_VALUE_CALLBACK;
        private Callback<? super DoubleGeneComponent, Double> mutation = DEFAULT_MUTATION_CALLBACK;
        private Callback<? super DoubleGeneComponent, ? extends Product2<Double, Double>> recombination = DEFAULT_RECOMBINATION_CALLBACK;

        public T initialAllele(Callback<? super DoubleGeneComponent, Double> callback) {
            this.initialValue = checkNotNull(callback);
            return self();
        }

        public T mutation(Callback<? super DoubleGeneComponent, Double> callback) {
            this.mutation = checkNotNull(callback);
            return self();
        }

        public T recombination(Callback<? super DoubleGeneComponent, ? extends Product2<Double, Double>> callback) {
            this.recombination = checkNotNull(callback);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            if (initialValue == DEFAULT_INITIAL_VALUE_CALLBACK)
                LOGGER.warn("Using default initialValue callback: {}", DEFAULT_INITIAL_VALUE_CALLBACK);
            if (mutation == DEFAULT_MUTATION_CALLBACK)
                LOGGER.warn("Using default mutation callback: {}", DEFAULT_MUTATION_CALLBACK);
            if (recombination == DEFAULT_RECOMBINATION_CALLBACK)
                LOGGER.warn("Using default recombination callback: {}", DEFAULT_RECOMBINATION_CALLBACK);
        }
    }
}
