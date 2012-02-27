package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.ComponentList;

public interface Chromosome<E extends Gene<?>> extends ComponentList<E> {
    double distance(Chromosome<? extends E> chromosome);
    public Iterable<E> findCopiesFor(final Iterable<? extends E> thisGenes);
    boolean isCompatibleGenome(Chromosome<? extends Gene<?>> chromosome);
    void updateAllGenes(Chromosome<? extends E> chromosome);
    void initGenes();
}
