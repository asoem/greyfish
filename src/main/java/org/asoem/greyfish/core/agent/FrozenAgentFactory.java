package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.utils.base.DeepCloner;

/**
* User: christoph
* Date: 12.11.12
* Time: 12:12
*/
public class FrozenAgentFactory<A extends Agent<A, ?>> implements CloneFactory<A> {
    @Override
    public A cloneAgent(A prototype) {
        final A clone = (A) DeepCloner.clone(prototype);
        return FrozenAgent.builder(clone.getPopulation())
                .addActions(clone.getActions())
                .addProperties(clone.getProperties())
                .addTraits(clone.getTraits())
                .build();
    }
}
