package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.individual.ComponentList;

import java.util.List;

public interface GeneComponentList<E extends GeneComponent<?>> extends ComponentList<E> {
    boolean isCompatible(GeneComponentList<? extends GeneComponent<?>> geneComponentList);
    void updateAllGenes(GeneComponentList<? extends E> geneComponentList);

    void initGenes();
    void updateGenes(List<?> values);

    ChromosomalHistory getOrigin();
    void setOrigin(ChromosomalHistory history);
}
