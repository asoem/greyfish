package org.asoem.greyfish.core.genes;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.utils.math.RandomUtils;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 27.04.12
 * Time: 10:44
 */
public final class Genes {

    private Genes() {}

    public static List<TraitVector<?>> recombine(final List<? extends TraitVector<?>> thisGenes, final List<? extends TraitVector<?>> thatGenes) {

        checkArgument(thisGenes.size() == thatGenes.size(), "TraitVector lists must have the same length");

        return ImmutableList.copyOf(new AbstractIterator<TraitVector<?>>() {

            private final Iterator<? extends TraitVector<?>> thisIterator = thisGenes.iterator();
            private final Iterator<? extends TraitVector<?>> thatIterator = thatGenes.iterator();

            private boolean thisOrThat = false;

            @Override
            protected TraitVector<?> computeNext() {

                if (!thisIterator.hasNext()) {
                    return endOfData();
                }

                assert thatIterator.hasNext();

                final TraitVector<?> thisTraitVector = thisIterator.next();
                final TraitVector<?> thatTraitVector = thatIterator.next();

                final double recombinationProbability = (thisOrThat ? thisTraitVector : thatTraitVector).getRecombinationProbability();
                if (recombinationProbability < 0 || recombinationProbability > 1)
                    throw new AssertionError("Recombination probability has an invalid value: " + recombinationProbability);

                final boolean recombine = RandomUtils.nextBoolean(recombinationProbability);
                if (recombine)
                    thisOrThat = !thisOrThat;

                return (thisOrThat ? thisTraitVector : thatTraitVector);
            }
        });
    }
}
