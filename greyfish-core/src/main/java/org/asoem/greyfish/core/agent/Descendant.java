package org.asoem.greyfish.core.agent;

import java.util.Set;

public interface Descendant {
    Set<Integer> getParents();

    void setParents(Set<Integer> parents);
}
