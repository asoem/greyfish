package org.asoem.greyfish.core.model;

import com.google.common.collect.Lists;
import org.asoem.greyfish.core.environment.Environment;

import java.util.List;

public abstract class AbstractExperiment implements Experiment {
    private List<SimulationListener> listeners = Lists.newCopyOnWriteArrayList();

    @Override
    public final void addSimulationListener(final SimulationListener listener) {
        this.listeners.add(listener);
    }

    protected final void notifyStarted(final Environment<?> environment) {
        for (SimulationListener listener : listeners) {
            listener.started(environment);
        }
    }

    protected final void notifyDone(final Environment<?> environment) {
        for (SimulationListener listener : listeners) {
            listener.done(environment);
        }
    }
}
