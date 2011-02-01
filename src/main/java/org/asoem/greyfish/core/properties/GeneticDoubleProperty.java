package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.genes.DoubleGene;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;

@ClassGroup(tags="property")
public final class GeneticDoubleProperty extends PropertyDecorator implements DiscreteProperty<Double>, OrderedSet<Double> {

    private final DoubleProperty delegate;

    private Supplier<Double> doubleSupplier = new Supplier<Double>() {
        @Override
        public Double get() {
            return delegate.getValue();
        }
    };

    protected GeneticDoubleProperty(Builder builder) {
        delegate = new DoubleProperty(builder);
    }

    protected GeneticDoubleProperty(GeneticDoubleProperty geneticDoubleProperty, CloneMap cloneMap) {
        delegate = cloneMap.clone(geneticDoubleProperty.delegate, DoubleProperty.class);
    }

    @Override
    public Double getValue() {
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
                new DoubleGene(delegate.getInitialValue(), getLowerBound(), getUpperBound()), Double.class);
    }

    public static Builder with() { return new Builder(); }

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

    public static final class Builder extends DoubleProperty.AbstractBuilder<Builder> implements BuilderInterface<GeneticDoubleProperty> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public GeneticDoubleProperty build() { return new GeneticDoubleProperty(this); }
    }


}
