package org.asoem.greyfish.core.simulation;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 12:54
 */
public interface Simulatable {
    void activate(Simulation context);
    void execute();
    void shutDown();
}
