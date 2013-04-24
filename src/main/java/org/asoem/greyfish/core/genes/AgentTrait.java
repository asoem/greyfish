package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;

public interface AgentTrait<A extends Agent<A, ?>, T> extends AgentComponent<A>, Trait<T> {

    /**
     * @return the class of the value this gene is supplying
     */
    Class<? super T> getValueClass();

    /**
     * Get the recombination probability for this gene.
     * The values is uses as the probability that, if this gene is on the focal agentTraitList,
     * the gene on the non-focal agentTraitList. If the non-focal is taken, at the next gene, the focal agentTraitList will be the currently non-focal.
     */
    double getRecombinationProbability();

    T mutate(T allele);

    T segregate(T allele1, T allele2);

    T createInitialValue();

    void trySet(Object o) throws ClassCastException;

    boolean isHeritable();
}
