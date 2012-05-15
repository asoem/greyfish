package org.asoem.greyfish.core.properties;

import java.util.Set;

public interface FiniteStateProperty<T> extends GFProperty<T> {
    Set<T> getStates();
}
