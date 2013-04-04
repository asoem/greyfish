package org.asoem.greyfish.core.agent;

/**
 * User: christoph
 * Date: 09.11.12
 * Time: 17:21
 */
public interface CloneFactory<A extends Agent<A, ?>> {
    A cloneAgent(A prototype);
}
