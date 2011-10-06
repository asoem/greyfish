package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.ImmutableGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.GeneController;
import org.asoem.greyfish.core.genes.GeneControllerAdaptor;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.lang.ImmutableBitSet;
import org.asoem.greyfish.utils.DeepCloner;
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

    protected BitSetTrait(BitSetTrait doubleOrderedSetProperty, DeepCloner cloner) {
        super(doubleOrderedSetProperty, cloner);
        bitStringGene = registerGene(ImmutableGene.newMutatedCopy(doubleOrderedSetProperty.bitStringGene));
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new BitSetTrait(this, cloner);
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

    protected BitSetTrait(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);

        GeneController<ImmutableBitSet> mutationOperator = new GeneControllerAdaptor<ImmutableBitSet>() {
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
        };

        final ImmutableBitSet bitSet = (builder.initialValue == null) ? new ImmutableBitSet(DEFAULT_BITSET_LENGTH, RandomUtils.randomInstance()) : builder.initialValue;

        bitStringGene = registerGene(
            new ImmutableGene<ImmutableBitSet>(bitSet, ImmutableBitSet.class, mutationOperator));
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<BitSetTrait> {
        @Override protected Builder self() { return this; }
        @Override public BitSetTrait build() { return new BitSetTrait(checkedSelf()); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFProperty.AbstractBuilder<T> {
        protected ImmutableBitSet initialValue;

        public T initialValue(ImmutableBitSet initialValue) { this.initialValue = initialValue; return self(); }
    }
}
