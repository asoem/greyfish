package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.space.Object2D;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;

/**
 * A SimulationLogger defines methods to write getSimulation events to a given destination of type {@link Closeable}.
 */
public interface SimulationLogger extends Closeable {

    /**
     * Log a getSimulation instance.
     */
    void logSimulation(final Simulation<?> simulation);

    void logAgentCreation(int agentId, String prototypeGroupName, int activationStep,
                          String simulationName, Set<Integer> parents,
                          Map<String, ?> traitValues);

    void logAgentEvent(int currentStep, String source, String title, String message, int agentId, Object2D projection);

    void logAgentInteraction(int sourceAgentId, int targetAgentId, String type, int simulationStep);

    /**
     * Log a property of type {@code marker}
     *
     * @param marker the type of the property
     * @param key    the key of the property
     * @param value  the value of the property
     */
    void logProperty(String marker, String key, String value);
}
