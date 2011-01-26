package org.asoem.greyfish.core.properties;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.individual.NamedDeepCloneableIndividualComponent;
import org.asoem.greyfish.utils.ConfigurableValueProvider;

public interface GFProperty extends NamedDeepCloneableIndividualComponent, ConfigurableValueProvider {
	public void mutate();

    public ImmutableList<? extends Gene<?>> getGeneList();
}
