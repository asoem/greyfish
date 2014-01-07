package org.asoem.greyfish.core.traits;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * A {@code Chromosome} implementation which stores and injects only heritable traits.
 */
public final class HeritableTraitsChromosome implements Chromosome {

    private final List<TraitVector<?>> traitVectors;

    public HeritableTraitsChromosome(final Iterable<? extends TraitVector<?>> genes) {
        this.traitVectors = ImmutableList.copyOf(genes);
    }

    @Override
    public List<TraitVector<?>> getTraitVectors() {
        return traitVectors;
    }

    @Override
    public int size() {
        return traitVectors.size();
    }

}
