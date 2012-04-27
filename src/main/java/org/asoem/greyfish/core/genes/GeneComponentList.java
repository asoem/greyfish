package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.ComponentList;

public interface GeneComponentList<E extends GeneComponent<?>> extends ComponentList<E> {
    double distance(GeneComponentList<? extends E> geneComponentList);
    boolean isCompatible(GeneComponentList<? extends GeneComponent<?>> geneComponentList);
    void updateAllGenes(GeneComponentList<? extends E> geneComponentList);

    void initGenes();
    void updateGenes(Iterable<?> values);

    ChromosomalOrigin getOrigin();
    void setOrigin(ChromosomalOrigin origin);
}
