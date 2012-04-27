package org.asoem.greyfish.core.genes;

import java.util.Collections;
import java.util.Set;

/**
 * User: christoph
 * Date: 26.04.12
 * Time: 15:38
 */
public enum NoChromosomalOrigin implements ChromosomalOrigin {
    INSTANCE;

    @Override
    public Set<Integer> getParents() {
        return Collections.emptySet();
    }
}
