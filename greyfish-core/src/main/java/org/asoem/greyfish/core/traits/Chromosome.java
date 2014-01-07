package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.core.agent.Agent;

import java.util.List;

/**
 * A Chromosome is a carrier for {@link TraitVector}s and used to transmit values of heritable {@link Trait}s from one
 * {@link Agent} to another.
 */
public interface Chromosome {

    /**
     * Get the trait vectors for this chromosome.
     *
     * @return a list of trait vectors
     */
    List<TraitVector<?>> getTraitVectors();

    /**
     * Get the size of this chromosome, which is the equal to the size of the trait vector {@link #getTraitVectors()}.
     *
     * @return the size of this chromosome
     */
    int size();

}
