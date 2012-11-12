package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.utils.base.DeepCloner;

/**
* User: christoph
* Date: 12.11.12
* Time: 12:12
*/
public class FrozenAgentFactory implements CloneFactory<Agent> {
    @Override
    public Agent cloneAgent(Agent prototype) {
        final Agent clone = DeepCloner.clone(prototype, Agent.class);
        return FrozenAgent.builder(clone.getPopulation())
                .addActions(clone.getActions())
                .addProperties(clone.getProperties())
                .addTraits(clone.getTraits())
                .build();
    }
}
