package org.asoem.greyfish.core.properties;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.GeneControllerAdaptor;
import org.asoem.greyfish.core.genes.ImmutableGene;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.ImmutableBitSet;
import org.asoem.greyfish.utils.math.RandomUtils;

/**
 * User: christoph
 * Date: 23.05.11
 * Time: 11:28
 */
@ClassGroup(tags = {"properties"})
public class DoubleTrait extends AbstractGFProperty implements RangeElementProperty<Double> {

    private final Gene<Double> doubleGene;
    private static final Double LOWER_BOUND = 0.0;
    private static final Double UPPER_BOUND = 100.0;

    @SimpleXMLConstructor
    private DoubleTrait() {
        this(new Builder());
    }

    protected DoubleTrait(DoubleTrait doubleTrait, DeepCloner map) {
        super(doubleTrait, map);
        doubleGene = registerGene(ImmutableGene.newMutatedCopy(doubleTrait.doubleGene));
    }

    protected DoubleTrait(AbstractBuilder<?,?> builder) {
        super(builder);
        doubleGene = registerGene(new ImmutableGene<Double>(
                RandomUtils.uniform(getRange()),
                Double.class,
                new GeneControllerAdaptor<Double>() {
                    @Override
                    public Double mutate(Double original) {
                        return original += RandomUtils.RANDOM_DATA.nextGaussian(0,1);
                    }

                    @Override
                    public double normalizedDistance(Double orig, Double copy) {
                        return Math.abs(orig - copy) / (UPPER_BOUND - LOWER_BOUND);
                    }

                    @Override
                    public Double createInitialValue() {
                        return RandomUtils.uniform(getRange());
                    }
                }
        ));
    }

    @Override
    public Double get() {
        return doubleGene.get();
    }

    @Override
    public Range<Double> getRange() {
        return Ranges.closed(LOWER_BOUND, UPPER_BOUND);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new DoubleTrait(this, cloner);
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<DoubleTrait, Builder>  {
        @Override protected Builder self() { return this; }
        @Override public DoubleTrait checkedBuild() { return new DoubleTrait(this); }
    }

    protected static abstract class AbstractBuilder<E extends DoubleTrait, T extends AbstractBuilder<E,T>> extends AbstractGFProperty.AbstractBuilder<E,T> {
        protected ImmutableBitSet initialValue;

        public T initialValue(ImmutableBitSet initialValue) { this.initialValue = initialValue; return self(); }
    }
}
