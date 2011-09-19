package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 15:59
 */
public abstract class ForwardingGenome extends ForwardingComponentList<Gene<?>> implements Genome {

    protected abstract Genome delegate();

    @Override
    public double distance(Genome genome) {
        return delegate().distance(genome);
    }

    @Override
    public Iterable<Gene<?>> findCopiesFor(Iterable<Gene<?>> thisGenes) {
        return delegate().findCopiesFor(thisGenes);
    }

    @Override
    public Genome mutated() {
        return delegate().mutated();
    }

    @Override
    public Genome recombined(Genome genome) {
        return delegate().recombined(genome);
    }
}
