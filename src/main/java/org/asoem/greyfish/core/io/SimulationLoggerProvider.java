package org.asoem.greyfish.core.io;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.asoem.greyfish.core.simulation.Simulation;

import java.util.Map;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 13:27
 */
public class SimulationLoggerProvider {

    @Inject
    private static SimulationLoggerFactory loggerFactory;

    private static final Map<Simulation, SimulationLogger> LOGGER_MAP = Maps.newHashMap();
    
    public static SimulationLogger getLogger(Simulation simulation) {
        if (! LOGGER_MAP.containsKey(simulation))
            LOGGER_MAP.put(simulation, loggerFactory.getLogger(simulation));
        return LOGGER_MAP.get(simulation);
    }
}
