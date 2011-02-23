package org.asoem.greyfish.core.properties;

import com.google.common.base.Supplier;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.IndexedGene;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.NamedDeepCloneableIndividualComponent;
import org.asoem.greyfish.utils.ConfigurableValueProvider;

import java.util.ListIterator;

public interface GFProperty extends GFComponent, NamedDeepCloneableIndividualComponent, ConfigurableValueProvider {

    /**
     * Add {@code gene} to this property which will than be recognized as part of the genome of this property's componentOwner
     *
     *
     * @param gene The gene to register in this property
     * @return A Supplier for type {@code S}
     * @see org.asoem.greyfish.core.individual.IndividualInterface#getGenome()
     */
    <S> Supplier<S> registerGene(final Gene<S> gene);

    /**
     * @return All registered genes wrapped in an {@code IndexedGene}
     * @see #registerGene(org.asoem.greyfish.core.genes.Gene)
     */
    public Iterable<IndexedGene<?>> getGenes();

    /**
     * Set the delegates of the contained {@code IndexedGene}s and their index to the values provided by the given {@code geneIterator}
     * @param geneIterator A {@code Genome}'s geneList ListIterator which provides the delegate genes
     * @see org.asoem.greyfish.core.genes.Genome#listIterator()
     */
    public void setGenes(ListIterator<? extends Gene<?>> geneIterator);
}
