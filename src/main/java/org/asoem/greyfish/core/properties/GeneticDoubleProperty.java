package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.genes.ImmutableGene;
import org.asoem.greyfish.core.genes.GeneControllerAdaptor;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.RandomUtils;

@ClassGroup(tags="property")
public final class GeneticDoubleProperty extends PropertyDecorator implements WellOrderedSetElementProperty<Double> {

    private final DoubleProperty delegate;

    private Supplier<Double> doubleSupplier = new Supplier<Double>() {
        @Override
        public Double get() {
            return delegate.get();
        }
    };

    protected GeneticDoubleProperty(GeneticDoubleProperty geneticDoubleProperty, DeepCloner cloner) {
        delegate = cloner.cloneField(geneticDoubleProperty.delegate, DoubleProperty.class);
    }

    @Override
    public Double get() {
        return doubleSupplier.get();
    }

    @Override
    protected GFProperty getDelegate() {
        return delegate;
    }

    @Override
    public void prepare(ParallelizedSimulation simulation) {
        delegate.prepare(simulation);
        doubleSupplier = delegate.registerGene(
                new ImmutableGene<Double>(delegate.getInitialValue(), Double.class, new GeneControllerAdaptor<Double>() {
                    @Override
                    public Double mutate(Double original) {
                        return RandomUtils.RANDOM_DATA.nextGaussian(0, 1);
                    }

                    @Override
                    public double normalizedDistance(Double orig, Double copy) {
                        return Math.abs(orig - copy) / (getUpperBound() - getLowerBound());
                    }
                }));
    }

    @Override
    public GeneticDoubleProperty deepClone(DeepCloner cloner) {
        return new GeneticDoubleProperty(this, cloner);
    }

    @Override
    public Double getUpperBound() {
        return delegate.getUpperBound();
    }

    @Override
    public Double getLowerBound() {
        return delegate.getLowerBound();
    }

    protected GeneticDoubleProperty(Builder builder) {
        delegate = new DoubleProperty(builder);
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends DoubleProperty.AbstractBuilder<Builder> implements BuilderInterface<GeneticDoubleProperty> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public GeneticDoubleProperty build() { return new GeneticDoubleProperty(this); }
    }


}
