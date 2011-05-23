package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.DefaultGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.MutationOperatorAdaptor;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.lang.ImmutableBitSet;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.RandomUtils;

/**
 * User: christoph
 * Date: 23.05.11
 * Time: 11:28
 */
@ClassGroup(tags = {"property"})
public class DoubleTrait extends AbstractGFProperty implements WellOrderedSetElementProperty<Double> {

    private final Gene<Double> doubleGene;
    private static final Double LOWER_BOUND = 0.0;
    private static final Double UPPER_BOUND = 100.0;

    @SimpleXMLConstructor
    private DoubleTrait() {
        this(new Builder());
    }

    protected DoubleTrait(DoubleTrait doubleTrait, CloneMap map) {
        super(doubleTrait, map);
        doubleGene = registerGene(DefaultGene.newMutatedCopy(doubleTrait.doubleGene));
    }

    protected DoubleTrait(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        doubleGene = registerGene(new DefaultGene<Double>(
                RandomUtils.RANDOM_DATA.nextUniform(getLowerBound(), getUpperBound()),
                Double.class,
                new MutationOperatorAdaptor<Double>() {
                    @Override
                    public Double mutate(Double original) {
                        return original += RandomUtils.RANDOM_DATA.nextGaussian(0,1);
                    }

                    @Override
                    public double normalizedDistance(Double orig, Double copy) {
                        return Math.abs(orig - copy) / (UPPER_BOUND - LOWER_BOUND);
                    }
                }
        ));
    }

    @Override
    public Double getUpperBound() {
        return UPPER_BOUND;
    }

    @Override
    public Double getLowerBound() {
        return LOWER_BOUND;
    }

    @Override
    public Double get() {
        return doubleGene.get();
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new DoubleTrait(this, map);
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<DoubleTrait> {
        @Override protected Builder self() { return this; }
        @Override public DoubleTrait build() { return new DoubleTrait(checkedSelf()); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFProperty.AbstractBuilder<T> {
        protected ImmutableBitSet initialValue;

        public T initialValue(ImmutableBitSet initialValue) { this.initialValue = initialValue; return self(); }
    }
}
