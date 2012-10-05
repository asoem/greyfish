package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.agent.ComponentList;

import java.util.List;

public interface GeneComponentList<E extends AgentTrait<?>> extends ComponentList<E> {
    boolean isCompatible(GeneComponentList<? extends AgentTrait<?>> geneComponentList);
    void updateAllGenes(GeneComponentList<? extends E> geneComponentList);

    void initGenes();
    void updateGenes(List<?> values);

    ChromosomalHistory getOrigin();
    void setOrigin(ChromosomalHistory history);

    int indexOfNamed(String name);
}
