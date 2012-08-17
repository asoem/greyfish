package org.asoem.greyfish.core.io;

import com.google.inject.Inject;
import org.asoem.greyfish.core.simulation.Simulation;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 13:27
 */
public class SimulationLoggerFactoryHolder {

    @Inject
    private static SimulationLoggerFactory loggerFactory;
    
    public static SimulationLogger getLogger(Simulation simulation) {
        return loggerFactory.getLogger(simulation);
    }
}
