package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.agent.Agent;

import java.util.List;
import java.util.Set;

/**
 * User: christoph
 * Date: 20.08.12
 * Time: 09:35
 */
public interface Chromosome {

    List<TraitVector<?>> getTraitVectors();

    Set<Integer> getParents();

    int size();

    public <A extends Agent<A, ?>> void updateAgent(Agent<A, ?> agent);
}
