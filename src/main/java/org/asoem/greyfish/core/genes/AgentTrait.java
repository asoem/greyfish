package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;

import javax.annotation.Nullable;


public interface AgentTrait<T, A extends Agent<?,A,?>> extends AgentComponent<A>, GeneLike<T> {

    /**
     * @return the class of the value this gene is supplying
     */
    Class<T> getAlleleClass();

    /**
     * @param gene the gene to builderTest for
     * @return {@code true} if {@code gene} is a mutated copy of this gene, {@code false} otherwise
     */
    boolean isMutatedCopy(@Nullable AgentTrait<?, A> gene);

    /**
     * Set the new value for this {@code AgentTrait}
     *
     * @param allele the allele
     */
    void setAllele(Object allele);

    /**
     * Get the recombination probability for this gene.
     * The values is uses as the probability that, if this gene is on the focal agentTraitList,
     * the gene on the non-focal agentTraitList. If the non-focal is taken, at the next gene, the focal agentTraitList will be the currently non-focal.
     */
    double getRecombinationProbability();

    T mutate(T allele);

    T segregate(T allele1, T allele2);

    T createInitialValue();
}
