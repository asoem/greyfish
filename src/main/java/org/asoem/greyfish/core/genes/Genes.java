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
public class Genes {
    public static List<Gene<?>> recombine(final List<? extends Gene<?>> thisGenes, final List<? extends Gene<?>> thatGenes) {

        checkArgument(thisGenes.size() == thatGenes.size(), "Gene lists must have the same length");

        return ImmutableList.copyOf(new AbstractIterator<Gene<?>>() {

            private final Iterator<? extends Gene<?>> thisIterator = thisGenes.iterator();
            private final Iterator<? extends Gene<?>> thatIterator = thatGenes.iterator();

            private boolean thisOrThat = false;

            @Override
            protected Gene<?> computeNext() {

                if (!thisIterator.hasNext()) {
                    return endOfData();
                }

                assert thatIterator.hasNext();

                final Gene<?> thisGene = thisIterator.next();
                final Gene<?> thatGene = thatIterator.next();

                final double recombinationProbability = (thisOrThat ? thisGene : thatGene).getRecombinationProbability();
                if (recombinationProbability < 0 || recombinationProbability > 1)
                    throw new AssertionError("Recombination probability has an invalid value: " + recombinationProbability);

                final boolean recombine = RandomUtils.trueWithProbability(recombinationProbability);
                if (recombine)
                    thisOrThat = !thisOrThat;

                return (thisOrThat ? thisGene : thatGene);
            }
        });
    }
}
