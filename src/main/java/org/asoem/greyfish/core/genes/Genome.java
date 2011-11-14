package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.ComponentList;

public interface Genome<E extends Gene<?>> extends ComponentList<E> {
    double distance(Genome<? extends E> genome);
    public Iterable<E> findCopiesFor(final Iterable<? extends E> thisGenes);
    boolean isCompatibleGenome(Genome<? extends Gene<?>> genome);
}
