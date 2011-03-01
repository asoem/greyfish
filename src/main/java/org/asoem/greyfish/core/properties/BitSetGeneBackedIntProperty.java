package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.DefaultGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.MutationOperator;
import org.asoem.greyfish.core.utils.BitStringUtils;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.RandomUtils;
import org.uncommons.maths.binary.BitString;

/**
 * User: christoph
 * Date: 23.02.11
 * Time: 14:20
 */
@ClassGroup(tags = {"property"})
public class BitSetGeneBackedIntProperty extends AbstractGFProperty implements OrderedSetProperty<Double> {

    private final static int BITSTRINGLENGTH = 10;

    // CAVE! The final modifier requires that getUpperBound() and getLowerBound() return the same value in all deepClones!
    private final MutationOperator<BitString> mutationOperator = new MutationOperator<BitString>() {
        @Override
        public BitString mutate(BitString original) {
            return BitStringUtils.mutate(original.clone(), 0.1);
        }

        @Override
        public double normalizedDistance(BitString orig, BitString copy) {
            return orig.toNumber().subtract(copy.toNumber()).doubleValue() / (getUpperBound() - getLowerBound());
        }

        @Override
        public double normalizedWeightedDistance(BitString orig, BitString copy) {
            return normalizedDistance(orig, copy);
        }
    };

    private final Gene<BitString> bitStringGene;

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
        return bitStringGene.get().toNumber().doubleValue();
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
        bitStringGene = registerGene(
            new DefaultGene<BitString>(new BitString(BITSTRINGLENGTH, RandomUtils.RNG), BitString.class, mutationOperator));
    }

    public static Builder with() { return new Builder(); }

    public static final class Builder extends AbstractOrderedSetProperty.AbstractBuilder<Builder, Double> implements BuilderInterface<BitSetGeneBackedIntProperty> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public BitSetGeneBackedIntProperty build() { return new BitSetGeneBackedIntProperty(this); }
    }
}
