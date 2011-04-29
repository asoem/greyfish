package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.DefaultGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.MutationOperator;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.lang.ImmutableBitSet;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.RandomUtils;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 23.02.11
 * Time: 14:20
 */
@ClassGroup(tags = {"property"})
public class BitSetTrait extends AbstractGFProperty implements WellOrderedSetElementProperty<ImmutableBitSet> {

    private final static int DEFAULT_BITSET_LENGTH = 10;

    private final Gene<ImmutableBitSet> bitStringGene;

    @SimpleXMLConstructor
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
                return ImmutableBitSet.newMutatedCopy(original, 0.1);
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

        final ImmutableBitSet bitSet = (builder.initialValue == null) ? new ImmutableBitSet(DEFAULT_BITSET_LENGTH, RandomUtils.randomInstance()) : builder.initialValue;

        bitStringGene = registerGene(
            new DefaultGene<ImmutableBitSet>(bitSet, ImmutableBitSet.class, mutationOperator));
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<BitSetTrait> {
        @Override protected Builder self() { return this; }
        @Override public BitSetTrait build() { return new BitSetTrait(checkedSelf()); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractWellOrderedSetElementProperty.AbstractBuilder<T, ImmutableBitSet> {
        protected ImmutableBitSet initialValue;

        public T setInitialValue(ImmutableBitSet initialValue) { this.initialValue = initialValue; return self(); }
    }
}
