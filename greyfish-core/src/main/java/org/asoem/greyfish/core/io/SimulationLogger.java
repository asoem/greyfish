package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.simulation.Simulation;

import java.io.Closeable;

/**
 * A SimulationLogger defines methods to write getSimulation events to a given destination of type {@link Closeable}.
 */
public interface SimulationLogger<A extends Agent<?, ?>> extends Closeable {

    /**
     * Log a getSimulation instance.
     */
    void logSimulation(final Simulation<?> simulation);

    /**
     * Log the creation of an agent.
     * @param agent the agent which got created.
     */
    void logAgentCreation(A agent);

    /**
     * Log an arbitrary event inside an agent.
     * @param agent the agent which activated the event
     * @param currentStep the step at which the event was activated
     * @param source the source (class) of the event
     * @param title the title of the event
     * @param message the message for the event
     */
    void logAgentEvent(A agent, long currentStep, String source, String title, String message);

    /**
     * Log a property of type {@code marker}
     * @param marker the type of the property
     * @param key the key of the property
     * @param value the value of the property
     */
    void logProperty(String marker, String key, String value);
}
