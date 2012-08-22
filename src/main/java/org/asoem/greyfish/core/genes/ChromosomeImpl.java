package org.asoem.greyfish.core.genes;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.Agent;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 25.04.12
 * Time: 15:27
 */
public class ChromosomeImpl implements Chromosome {

    private final List<Gene<?>> genes;
    private final ChromosomalHistory history;

    public ChromosomeImpl(ChromosomalHistory history, Iterable<? extends Gene<?>> genes) {
        this.history = checkNotNull(history);
        this.genes = ImmutableList.copyOf(genes);
    }

    @Override
    public ChromosomalHistory getHistory() {
        return history;
    }

    @Override
    public List<Gene<?>> getGenes() {
        return genes;
    }

    public ChromosomeImpl recombined(Chromosome other) {
        return new ChromosomeImpl(
                ChromosomalOrigins.merge(this.history, other.getHistory()),
                Genes.recombine(this.genes, other.getGenes()));
    }

    public static ChromosomeImpl forAgent(Agent agent) {
        checkNotNull(agent);
        return new ChromosomeImpl(
                new UniparentalChromosomalHistory(agent.getId()),
                Iterables.transform(agent.getGeneComponentList(), new Function<GeneComponent<?>, Gene<?>>() {
                    @Override
                    public Gene<?> apply(GeneComponent<?> input) {
                        return new Gene<Object>(input.getAllele(), 0);
                    }
                }));
    }
}
