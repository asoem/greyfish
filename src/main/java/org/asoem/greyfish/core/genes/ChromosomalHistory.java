package org.asoem.greyfish.core.genes;

import java.util.Set;

/**
 * User: christoph
 * Date: 26.04.12
 * Time: 15:33
 */

public interface ChromosomalHistory {
    Set<Integer> getParents();

    int size();
}
