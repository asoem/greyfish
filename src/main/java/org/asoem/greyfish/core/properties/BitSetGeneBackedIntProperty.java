package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.DefaultGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.MutationOperator;
import org.asoem.greyfish.core.utils.HammingDistanceComparable;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.lang.ImmutableBitSet;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.RandomUtils;

import static com.google.common.collect.Iterables.toArray;

/**
 * User: christoph
 * Date: 23.02.11
 * Time: 14:20
 */
@ClassGroup(tags = {"property"})
public class BitSetGeneBackedIntProperty extends AbstractGFProperty implements WellOrderedSetElementProperty<Double>, HammingDistanceComparable {

    private final static int BITSTRINGLENGTH = 10;

    private final Gene<ImmutableBitSet> bitStringGene;

    @SuppressWarnings("unused") // used in the deserialization process
    private BitSetGeneBackedIntProperty() {
        this(new Builder());
    }

    protected BitSetGeneBackedIntProperty(BitSetGeneBackedIntProperty doubleOrderedSetProperty, CloneMap cloneMap) {
        super(doubleOrderedSetProperty, cloneMap);
        bitStringGene = registerGene(DefaultGene.newMutatedCopy(doubleOrderedSetProperty.bitStringGene));
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new BitSetGeneBackedIntProperty(this, map);
    }


    @Override
    public Double get() {
        return bitStringGene.get().doubleValue();
    }

    @Override
    public Double getUpperBound() {
        return (double) (1 << BITSTRINGLENGTH) - 1;
    }

    @Override
    public Double getLowerBound() {
        return 0.0;
    }

    protected BitSetGeneBackedIntProperty(Builder builder) {
        super(builder);
        MutationOperator<ImmutableBitSet> mutationOperator = new MutationOperator<ImmutableBitSet>() {
            @Override
            public ImmutableBitSet mutate(ImmutableBitSet original) {
                return new ImmutableBitSet(original, 0.1);
            }

            @Override
            public double normalizedDistance(ImmutableBitSet orig, ImmutableBitSet copy) {
                return Math.abs(orig.subtract(copy).doubleValue()) / (getUpperBound() - getLowerBound());
            }

            @Override
            public double normalizedWeightedDistance(ImmutableBitSet orig, ImmutableBitSet copy) {
                return normalizedDistance(orig, copy);
            }
        };
        bitStringGene = registerGene(
            new DefaultGene<ImmutableBitSet>(new ImmutableBitSet(BITSTRINGLENGTH, RandomUtils.RNG), ImmutableBitSet.class, mutationOperator));
    }

    public static Builder with() { return new Builder(); }

    @Override
    public Object[] getHammingString() {
        return toArray(bitStringGene.get(), Boolean.class);
    }

    @Override
    public int hammingDistance(HammingDistanceComparable b) {
        return 0;
    }

    public static final class Builder extends AbstractWellOrderedSetElementProperty.AbstractBuilder<Builder, Double> implements BuilderInterface<BitSetGeneBackedIntProperty> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public BitSetGeneBackedIntProperty build() { return new BitSetGeneBackedIntProperty(this); }
    }
}
