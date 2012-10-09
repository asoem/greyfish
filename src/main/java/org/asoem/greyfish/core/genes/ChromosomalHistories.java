package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

/**
 * User: christoph
 * Date: 08.10.12
 * Time: 18:05
 */
public final class ChromosomalHistories {

    private ChromosomalHistories() {}

    public static ChromosomalHistory emptyHistory() {
        return NoChromosomalHistory.INSTANCE;
    }

    public static ChromosomalHistory uniparentalHistory(int parent) {
        return new GenericChromosomalHistory(ImmutableSet.of(parent));
    }

    public static ChromosomalHistory biparentalHistory(int parent1, int parent2) {
        return new GenericChromosomalHistory(ImmutableSet.of(parent1, parent2));
    }

    public static ChromosomalHistory merge(final ChromosomalHistory history, final ChromosomalHistory history1) {
        return new GenericChromosomalHistory(Sets.union(history.getParents(), history1.getParents()));
    }

    private static enum NoChromosomalHistory implements ChromosomalHistory {
        INSTANCE;

        @Override
        public Set<Integer> getParents() {
            return Collections.emptySet();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    private static class GenericChromosomalHistory implements ChromosomalHistory {

        private final Set<Integer> parents;

        private GenericChromosomalHistory(Set<Integer> parents) {
            assert parents != null;
            this.parents = parents;
        }

        @Override
        public Set<Integer> getParents() {
            return parents;
        }

        @Override
        public int size() {
            return parents.size();
        }
    }
}
