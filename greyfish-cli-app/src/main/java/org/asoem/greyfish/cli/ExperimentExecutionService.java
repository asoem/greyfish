package org.asoem.greyfish.cli;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import org.asoem.greyfish.core.model.Experiment;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This service executes a given {@link org.asoem.greyfish.core.simulation.DiscreteTimeSimulation}.
 */
final class ExperimentExecutionService extends AbstractExecutionThreadService {

    private final Experiment experiment;

    @Inject
    private ExperimentExecutionService(final Experiment experiment) {
        this.experiment = experiment;
    }

    @Override
    protected void run() throws Exception {
        experiment.run();
    }

    public Experiment getExperiment() {
        return experiment;
    }
}
