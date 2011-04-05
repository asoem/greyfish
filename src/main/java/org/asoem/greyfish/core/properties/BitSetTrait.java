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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.toArray;

/**
 * User: christoph
 * Date: 23.02.11
 * Time: 14:20
 */
@ClassGroup(tags = {"property"})
public class BitSetTrait extends AbstractGFProperty implements WellOrderedSetElementProperty<ImmutableBitSet>, HammingDistanceComparable {

    private final static int BITSET_LENGTH = 10;

    private final Gene<ImmutableBitSet> bitStringGene;

    @SuppressWarnings("unused") // used in the deserialization process
    private BitSetTrait() {
        this(new Builder());
    }

    protected BitSetTrait(BitSetTrait doubleOrderedSetProperty, CloneMap cloneMap) {
        super(doubleOrderedSetProperty, cloneMap);
        bitStringGene = registerGene(DefaultGene.newMutatedCopy(doubleOrderedSetProperty.bitStringGene));
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new BitSetTrait(this, map);
    }


    @Override
    public ImmutableBitSet get() {
        return bitStringGene.get();
    }

    @Override
    public ImmutableBitSet getUpperBound() {
        return ImmutableBitSet.ones(bitStringGene.get().length());
    }

    @Override
    public ImmutableBitSet getLowerBound() {
        return ImmutableBitSet.zeros(bitStringGene.get().length());
    }

    protected BitSetTrait(Builder builder) {
        super(builder);
        MutationOperator<ImmutableBitSet> mutationOperator = new MutationOperator<ImmutableBitSet>() {
            @Override
            public ImmutableBitSet mutate(ImmutableBitSet original) {
                checkNotNull(original);
                return new ImmutableBitSet(original, 0.1);
            }

            @Override
            public double normalizedDistance(ImmutableBitSet orig, ImmutableBitSet copy) {
                checkNotNull(orig);
                checkNotNull(copy);
                return (double) orig.hammingDistance(copy) / Math.max(orig.length(), copy.length());
            }

            @Override
            public double normalizedWeightedDistance(ImmutableBitSet orig, ImmutableBitSet copy) {
                return normalizedDistance(orig, copy);
            }
        };
        bitStringGene = registerGene(
            new DefaultGene<ImmutableBitSet>(new ImmutableBitSet(BITSET_LENGTH, RandomUtils.RNG), ImmutableBitSet.class, mutationOperator));
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

    public static final class Builder extends AbstractWellOrderedSetElementProperty.AbstractBuilder<Builder, ImmutableBitSet> implements BuilderInterface<BitSetTrait> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public BitSetTrait build() { return new BitSetTrait(this); }
    }
}
