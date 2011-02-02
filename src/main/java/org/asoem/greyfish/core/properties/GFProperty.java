package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.GFComponent;
import org.asoem.greyfish.core.individual.NamedDeepCloneableIndividualComponent;
import org.asoem.greyfish.utils.ConfigurableValueProvider;

import java.util.Iterator;
import java.util.List;

public interface GFProperty extends GFComponent, NamedDeepCloneableIndividualComponent, ConfigurableValueProvider {
	public void mutate();

    public List<? extends Gene<?>> getGeneList();
    public void setGenes(Iterator<Gene<?>> geneIterator);
}
