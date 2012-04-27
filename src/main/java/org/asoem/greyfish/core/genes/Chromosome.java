package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableList;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 25.04.12
 * Time: 15:27
 */
public class Chromosome {

    final List<Gene<?>> genes;
    final ChromosomalOrigin origin;

    public Chromosome(ChromosomalOrigin origin, Iterable<? extends Gene<?>> genes) {
        this.origin = checkNotNull(origin);
        this.genes = ImmutableList.copyOf(genes);
    }

    public ChromosomalOrigin getOrigin() {
        return origin;
    }

    public Iterable<Gene<?>> getGenes() {
        return genes;
    }

    public Chromosome recombined(Chromosome other) {
        return new Chromosome(
                ChromosomalOrigins.merge(this.origin, other.origin),
                Genes.recombine(this.genes, other.genes));
    }
}
