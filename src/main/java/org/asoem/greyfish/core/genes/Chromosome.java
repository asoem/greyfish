package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.ComponentList;

public interface Chromosome<E extends Gene<?>> extends ComponentList<E> {
    double distance(Chromosome<? extends E> chromosome);
    boolean isCompatible(Chromosome<? extends Gene<?>> chromosome);
    void updateAllGenes(Chromosome<? extends E> chromosome);

    void initGenes();
    void updateGenes(Iterable<?> values);

    ChromosomalOrigin getOrigin();
    void setOrigin(ChromosomalOrigin origin);
}
