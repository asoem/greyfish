package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.ComponentList;

public interface Genome extends ComponentList<Gene<?>> {
    double distance(Genome genome);
    public Iterable<Gene<?>> findCopiesFor(final Iterable<Gene<?>> thisGenes);
    Genome mutated();
    Genome recombined(Genome genome);
}
