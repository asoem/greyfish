package org.asoem.greyfish.core.properties;

import java.util.Set;

public interface FiniteStateProperty<T> extends DiscreteProperty<T> {
    Set<T> getStates();
}
