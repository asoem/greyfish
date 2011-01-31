package org.asoem.greyfish.core.properties;

import com.google.common.collect.Ordering;
import com.jgoodies.validation.ValidationResult;
import org.asoem.greyfish.core.genes.DoubleGene;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.primitives.Doubles.asList;

@ClassGroup(tags="property")
public final class GeneticDoubleProperty extends AbstractGFProperty implements DiscreteProperty<Double>, OrderedSet<Double> {

    private DoubleGene gene = registerGene(new DoubleGene());

    protected GeneticDoubleProperty(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected GeneticDoubleProperty(GeneticDoubleProperty geneticDoubleProperty, CloneMap cloneMap) {
        super(geneticDoubleProperty, cloneMap);
    }

    @Override
    public void export(Exporter e) {
        e.addField(new ValueAdaptor<Double>("Min", Double.class, gene.getMin()) {

            @Override
            protected void writeThrough(Double arg0) {
                gene.setMin(checkFrozen(checkNotNull(arg0)));
            }
        });
        e.addField(new ValueAdaptor<Double>("Max", Double.class, gene.getMax()) {

            @Override
            protected void writeThrough(Double arg0) {
                gene.setMax(checkFrozen(checkNotNull(arg0)));
            }
        });
        e.addField(new ValueAdaptor<Double>("Initial", Double.class, gene.getRepresentation()) {

            @Override
            protected void writeThrough(Double arg0) {
                gene.setRepresentation(checkFrozen(checkNotNull(arg0)));
            }

            @Override
            public ValidationResult validate() {
                ValidationResult validationResult = new ValidationResult();
                if (!Ordering.natural().isOrdered(asList(gene.getMin(), gene.getMax(), gene.getRepresentation())))
                    validationResult.addError("Value of `Initial' must not be smaller than `Min' and greater than `Max'");
                return validationResult;
            }
        });
    }

    @Override
    public Double getValue() {
        return gene.getRepresentation();
    }

    @Override
    public Double getUpperBound() {
        return gene.getMax();
    }

    @Override
    public Double getLowerBound() {
        return gene.getMin();
    }

    public static Builder with() { return new Builder(); }

    @Override
    public GeneticDoubleProperty deepCloneHelper(CloneMap cloneMap) {
        return new GeneticDoubleProperty(this, cloneMap);
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<GeneticDoubleProperty> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public GeneticDoubleProperty build() { return new GeneticDoubleProperty(this); }
    }
}
