package org.asoem.greyfish.utils.space.cluster;

import org.asoem.greyfish.utils.space.DistanceMeasure;

import java.util.Collection;

public interface NeighborSearch<O> {
    Collection<O> filterNeighbors(final Collection<O> collection, O origin,
                                  final DistanceMeasure<? super O> distanceMeasure, final double range);
}
