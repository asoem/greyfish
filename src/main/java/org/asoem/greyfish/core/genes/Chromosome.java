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

    private final List<Gene<?>> genes;
    private final ChromosomalHistory history;

    public Chromosome(ChromosomalHistory history, Iterable<? extends Gene<?>> genes) {
        this.history = checkNotNull(history);
        this.genes = ImmutableList.copyOf(genes);
    }

    public ChromosomalHistory getHistory() {
        return history;
    }

    public List<Gene<?>> getGenes() {
        return genes;
    }

    public Chromosome recombined(Chromosome other) {
        return new Chromosome(
                ChromosomalOrigins.merge(this.history, other.history),
                Genes.recombine(this.genes, other.genes));
    }
}
