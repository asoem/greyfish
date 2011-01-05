package org.asoem.sico.core.properties;

import org.asoem.sico.core.genes.Gene;
import org.asoem.sico.core.individual.NamedDeepCloneableIndividualComponent;
import org.asoem.sico.utils.ConfigurableValueProvider;

public interface GFProperty extends NamedDeepCloneableIndividualComponent, ConfigurableValueProvider {
	public void mutate();
	public Gene<?>[] getGenes();
}
