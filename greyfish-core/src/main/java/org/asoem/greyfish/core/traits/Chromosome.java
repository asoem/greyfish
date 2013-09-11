package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.core.agent.Agent;

import java.util.List;
import java.util.Set;

/**
 * A Chromosome is a carrier for {@link TraitVector}s and used to transmit values
 * of heritable {@link Trait}s from one {@link Agent} to another.
 */
public interface Chromosome {

    /**
     * Get the trait vectors for this chromosome.
     * @return a list of trait vectors
     */
    List<TraitVector<?>> getTraitVectors();

    /**
     * Get the IDs of the agents which created this {@code Chromosome}.
     * @return a set of gent IDs
     * @see org.asoem.greyfish.core.agent.Agent#getId()
     */
    Set<Integer> getParents();

    /**
     * Get the size of this chromosome, which is the equal to the size of the trait vector {@link #getTraitVectors()}.
     * @return the size of this chromosome
     */
    int size();

    /**
     * Inject the values of the {@link TraitVector}s in {@link #getTraitVectors()}
     * into the appropriate {@link Trait}s of the given {@code agent}.
     * @see Trait#copyFrom(org.asoem.greyfish.utils.base.TypedSupplier)
     * @see Agent#setParents(java.util.Set)
     * @param agent the agent to update
     */
    <A extends Agent<A, ?>> void updateAgent(final A agent);
}
