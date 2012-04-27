package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * User: christoph
 * Date: 26.04.12
 * Time: 15:38
 */
public class UniparentalChromosomalOrigin implements ChromosomalOrigin {
    private Set<Integer> parent;

    public UniparentalChromosomalOrigin(int parent) {
        this.parent = ImmutableSet.of(parent);
    }

    @Override
    public Set<Integer> getParents() {
        return parent;
    }
}
