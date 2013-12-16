package org.asoem.greyfish.core.traits;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;

import javax.annotation.Nullable;

public interface AgentTrait<A extends Agent<A, ?>, T> extends AgentComponent, Trait<T> {

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

    @Override
    TypeToken<T> getValueType();

    @Override
    T get();
}
