package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * User: christoph
 * Date: 26.04.12
 * Time: 15:39
 */
public class BiparentalChromosomalHistory implements ChromosomalHistory {

    private final Set<Integer> parents;

    public BiparentalChromosomalHistory(int parent1, int parent2) {
        this.parents = ImmutableSet.of(parent1, parent2);
    }

    @Override
    public Set getParents() {
        return parents;
    }
}
