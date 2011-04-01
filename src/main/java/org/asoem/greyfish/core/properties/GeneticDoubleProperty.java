package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.genes.DefaultGene;
import org.asoem.greyfish.core.genes.MutationOperator;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.RandomUtils;
import org.uncommons.maths.random.GaussianGenerator;

@ClassGroup(tags="property")
public final class GeneticDoubleProperty extends PropertyDecorator implements WellOrderedSetElementProperty<Double> {

    private final DoubleProperty delegate;

    private Supplier<Double> doubleSupplier = new Supplier<Double>() {
        @Override
        public Double get() {
            return delegate.get();
        }
    };

    protected GeneticDoubleProperty(GeneticDoubleProperty geneticDoubleProperty, CloneMap cloneMap) {
        delegate = cloneMap.clone(geneticDoubleProperty.delegate, DoubleProperty.class);
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
    public void initialize(Simulation simulation) {
        delegate.initialize(simulation);
        doubleSupplier = delegate.registerGene(
                new DefaultGene<Double>(delegate.getInitialValue(), Double.class, new MutationOperator<Double>() {
                    @Override
                    public Double mutate(Double original) {
                        return new GaussianGenerator(getLowerBound(), getUpperBound(), RandomUtils.RNG).nextValue();
                    }

                    @Override
                    public double normalizedDistance(Double orig, Double copy) {
                        return Math.abs(orig - copy) / (getUpperBound() - getLowerBound());
                    }

                    @Override
                    public double normalizedWeightedDistance(Double orig, Double copy) {
                        return normalizedDistance(orig, copy);
                    }
                }));
    }

    @Override
    public GeneticDoubleProperty deepCloneHelper(CloneMap cloneMap) {
        return new GeneticDoubleProperty(this, cloneMap);
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
