package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.NamedDeepCloneableIndividualComponent;

public interface GFProperty extends GFComponent, NamedDeepCloneableIndividualComponent {

    /**
     * Add {@code gene} to this property which will than be recognized as part of the genome of this property's agent
     *
     *
     *
     * @param gene The gene to register in this property
     * @return A Supplier for type {@code S}
     * @see org.asoem.greyfish.core.individual.Agent#getGenome()
     */
    <S> Gene<S> registerGene(final Gene<S> gene);

    /**
     * @return All registered genes wrapped in an {@code IndexedGene}
     * @see #registerGene(org.asoem.greyfish.core.genes.Gene)
     */
    public Iterable<Gene<?>> getGenes();

    public void setGenes(Iterable<? extends Gene<?>> geneIterator);
}
