package org.asoem.greyfish.core.agent;

public interface CloneFactory<A extends Agent<?>> {
    A cloneAgent(A prototype);
}
