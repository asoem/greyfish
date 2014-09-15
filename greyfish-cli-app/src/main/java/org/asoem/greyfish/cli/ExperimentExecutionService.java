package org.asoem.greyfish.cli;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import org.asoem.greyfish.core.model.Experiment;

/**
 * This service executes a given {@link org.asoem.greyfish.core.simulation.DiscreteTimeEnvironment}.
 */
final class ExperimentExecutionService extends AbstractExecutionThreadService {

    private final Experiment experiment;

    @Inject
    private ExperimentExecutionService(final Experiment experiment, final EventBus eventBus) {
        this.experiment = experiment;
        eventBus.register(this);
    }

    @Override
    protected void run() throws Exception {
        experiment.run();
    }

    public Experiment getExperiment() {
        return experiment;
    }

    @Subscribe
    public void eventBusExceptionHandler(final Throwable throwable) {
        stopAsync();
    }
}
