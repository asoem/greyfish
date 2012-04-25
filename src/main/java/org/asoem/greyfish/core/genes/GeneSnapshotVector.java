package org.asoem.greyfish.core.genes;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * User: christoph
 * Date: 25.04.12
 * Time: 15:27
 */
public class GeneSnapshotVector {

    final int agentId;
    final List<GeneSnapshot<?>> alleles;

    public GeneSnapshotVector(int agentId, Iterable<? extends GeneSnapshot<?>> alleles) {
        this.agentId = agentId;
        this.alleles = ImmutableList.copyOf(alleles);
    }

    public int getAgentId() {
        return agentId;
    }

    public Iterable<GeneSnapshot<?>> getSnapshots() {
        return alleles;
    }


    public GeneSnapshotVector recombined(GeneSnapshotVector other) {
        return new GeneSnapshotVector(0, alleles);
    }
}
