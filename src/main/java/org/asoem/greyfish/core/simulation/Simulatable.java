package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.utils.base.Preparable;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 12:54
 */
public interface Simulatable extends Preparable<Simulation> {
    void execute();
    void shutDown();
}
