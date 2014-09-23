package org.asoem.greyfish.utils.space.cluster;

import java.util.Collection;

public interface NeighborSearch<O> {
    Collection<O> filterNeighbors(Collection<O> collection, O origin, double range);
}
