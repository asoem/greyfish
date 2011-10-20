package org.asoem.greyfish.core.properties;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.GeneController;
import org.asoem.greyfish.core.genes.GeneControllerAdaptor;
import org.asoem.greyfish.core.genes.ImmutableGene;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.ImmutableBitSet;
import org.asoem.greyfish.utils.math.RandomUtils;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.utils.collect.ImmutableBitSet.ones;
import static org.asoem.greyfish.utils.collect.ImmutableBitSet.zeros;

/**
 * User: christoph
 * Date: 23.02.11
 * Time: 14:20
 */
@ClassGroup(tags = {"property"})
public class BitSetTrait extends AbstractGFProperty implements RangeElementProperty<ImmutableBitSet> {

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
    public Range<ImmutableBitSet> getRange() {
        return Ranges.closed(zeros(DEFAULT_BITSET_LENGTH), ones(DEFAULT_BITSET_LENGTH));
    }

    protected BitSetTrait(AbstractBuilder<?,?> builder) {
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

    public static final class Builder extends AbstractBuilder<BitSetTrait,Builder>  {
        @Override protected Builder self() { return this; }
        @Override public BitSetTrait checkedBuild() { return new BitSetTrait(this); }
    }

    protected static abstract class AbstractBuilder<E extends BitSetTrait, T extends AbstractBuilder<E,T>> extends AbstractGFProperty.AbstractBuilder<E,T> {
        protected ImmutableBitSet initialValue;

        public T initialValue(ImmutableBitSet initialValue) { this.initialValue = initialValue; return self(); }
    }
}
