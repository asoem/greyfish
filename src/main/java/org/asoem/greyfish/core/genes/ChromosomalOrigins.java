package org.asoem.greyfish.core.genes;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * User: christoph
 * Date: 27.04.12
 * Time: 10:41
 */
public class ChromosomalOrigins {
    public static ChromosomalOrigin merge(final ChromosomalOrigin origin, final ChromosomalOrigin origin1) {
        return new ChromosomalOrigin() {

            private Set<Integer> union = Sets.union(origin.getParents(), origin1.getParents());

            @Override
            public Set<Integer> getParents() {
                return union;
            }
        };
    }
}
