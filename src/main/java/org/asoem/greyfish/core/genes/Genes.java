package org.asoem.greyfish.core.genes;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.size;

/**
 * User: christoph
 * Date: 22.02.11
 * Time: 11:43
 */
public class Genes {

    public static double normalizedDistance(Iterable<? extends Gene<?>> thisGenes, Iterable<? extends Gene<?>> thatGenes) {
        checkArgument( size(checkNotNull(thisGenes)) == size(checkNotNull(thatGenes)) );

        double ret = 0;

        final Iterator<? extends Gene<?>> other_genome_iter = thisGenes.iterator();
        final Iterator<? extends Gene<?>> this_genome_iter = thatGenes.iterator();

        while (this_genome_iter.hasNext() && other_genome_iter.hasNext()) {

            Gene<?> thisGene = this_genome_iter.next();
            Gene<?> thatGene = other_genome_iter.next();

//            if (!thisGene.isMutatedCopyOf(thatGene))
//                throw new IllegalArgumentException("Genes are not compatible: " + thisGene + ", " + thatGene);

            if (!thisGene.getSupplierClass().equals(thatGene.getSupplierClass()))
                throw new IllegalArgumentException("Genes implemented with different types: this: " + thisGene.getSupplierClass() + ", that: " + thatGene.getSupplierClass());

            ret += thisGene.distance(thatGene);
        }

        return ret;
    }
}
