package org.asoem.greyfish.core.model;

import com.google.common.collect.Lists;
import org.asoem.greyfish.core.simulation.Simulation;

import java.util.List;

public abstract class AbstractExperiment implements Experiment {
    private List<SimulationListener> listeners = Lists.newCopyOnWriteArrayList();

    @Override
    public final void addSimulationListener(final SimulationListener listener) {
        this.listeners.add(listener);
    }

    protected final void notifyStarted(final Simulation<?> simulation) {
        for (SimulationListener listener : listeners) {
            listener.started(simulation);
        }
    }

    protected final void notifyDone(final Simulation<?> simulation) {
        for (SimulationListener listener : listeners) {
            listener.done(simulation);
        }
    }
}
