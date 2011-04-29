package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.HasName;
import org.asoem.greyfish.utils.Preparable;

public interface NamedIndividualComponent extends GFComponent, Preparable<Simulation>, HasName {
}
