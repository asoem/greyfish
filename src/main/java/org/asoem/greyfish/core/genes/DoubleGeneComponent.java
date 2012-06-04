package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.individual.Callback;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.*;
import static org.asoem.greyfish.core.individual.Callbacks.call;

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
    private Callback<? super DoubleGeneComponent, Double> recombination;

    private final GeneController<Double> geneController = new GeneController<Double>() {

        @Override
        public Double mutate(Double original) {
            return mutation.apply(DoubleGeneComponent.this, ImmutableMap.of("original", original));
        }

        @Override
        public Double recombine(Double first, Double second) {
            return recombination.apply(DoubleGeneComponent.this, ImmutableMap.of("first", first, "second", second));
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
    public void setValue(Object value) {
        checkArgument(value instanceof Double);
        this.value = (Double) value;
    }

    @Override
    public Double getValue() {
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
        private Callback<? super DoubleGeneComponent, Double> initialValue;
        private Callback<? super DoubleGeneComponent, Double> mutation;
        private Callback<? super DoubleGeneComponent, Double> recombination;

        public T initialValue(Callback<? super DoubleGeneComponent, Double> expression) {
            this.initialValue = checkNotNull(expression);
            return self();
        }

        public T mutation(Callback<? super DoubleGeneComponent, Double> expression) {
            this.mutation = checkNotNull(expression);
            return self();
        }

        public T recombination(Callback<? super DoubleGeneComponent, Double> expression) {
            this.recombination = checkNotNull(expression);
            return self();
        }

        @Override
        protected void checkBuilder() throws IllegalStateException {
            super.checkBuilder();
            checkState(initialValue != null, "You must provide an expression for the initial value");
            checkState(mutation != null, "You must provide an expression for the mutation");
            checkState(recombination != null, "You must provide an expression for the recombination");
        }
    }
}
