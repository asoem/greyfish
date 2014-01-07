package org.asoem.greyfish.core.traits;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@code Chromosome} implementation which stores and injects only heritable traits.
 */
public final class HeritableTraitsChromosome implements Chromosome {

    private final List<TraitVector<?>> traitVectors;
    private final Set<Integer> parents;

    public HeritableTraitsChromosome(final Iterable<? extends TraitVector<?>> genes, final Set<Integer> parents) {
        this.traitVectors = ImmutableList.copyOf(genes);
        this.parents = checkNotNull(parents);
    }

    @Override
    public List<TraitVector<?>> getTraitVectors() {
        return traitVectors;
    }

    @Override
    public Set<Integer> getParents() {
        return parents;
    }

    @Override
    public int size() {
        return traitVectors.size();
    }

}
