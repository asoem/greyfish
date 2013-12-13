package org.asoem.greyfish.core.traits;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;
import org.asoem.greyfish.core.agent.SimulationContext;

import javax.annotation.Nullable;

public interface AgentTrait<A extends Agent<A, ? extends SimulationContext<?>>, T> extends AgentComponent, Trait<T> {

    /**
     * Get the recombination probability for this gene. The values is uses as the probability that, if this gene is on
     * the focal agentTraitList, the gene on the non-focal agentTraitList. If the non-focal is taken, at the next gene,
     * the focal agentTraitList will be the currently non-focal.
     */
    double getRecombinationProbability();

    T mutate(T allele);

    T segregate(T allele1, T allele2);

    T createInitialValue();

    boolean isHeritable();

    /**
     * Get the agent this component was added to.
     *
     * @return the agent for this component
     */
    Optional<A> agent();

    /**
     * Sets the connected agent. This method should only be called by an Agent implementation in an addXXX method.
     *
     * @param agent the new agent
     */
    void setAgent(@Nullable A agent);
}
