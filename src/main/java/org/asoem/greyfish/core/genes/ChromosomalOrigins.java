package org.asoem.greyfish.core.genes;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * User: christoph
 * Date: 27.04.12
 * Time: 10:41
 */
public class ChromosomalOrigins {
    public static ChromosomalHistory merge(final ChromosomalHistory history, final ChromosomalHistory history1) {
        return new ChromosomalHistory() {

            private Set<Integer> union = Sets.union(history.getParents(), history1.getParents());

            @Override
            public Set<Integer> getParents() {
                return union;
            }
        };
    }
}
