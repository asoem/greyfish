package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.NamedDeepCloneableIndividualComponent;
import org.asoem.greyfish.utils.ConfigurableValueProvider;

import java.util.ListIterator;

public interface GFProperty extends GFComponent, NamedDeepCloneableIndividualComponent, ConfigurableValueProvider {
	public void mutate();

    public Iterable<Gene<?>> getGenes();
    public void setGenes(ListIterator<Gene<?>> geneIterator);
}
