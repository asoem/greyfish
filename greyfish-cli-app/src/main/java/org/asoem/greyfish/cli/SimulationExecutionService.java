package org.asoem.greyfish.cli;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.asoem.greyfish.core.model.SimulationModel;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.simulation.Simulations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This service executes a given {@link Simulation}.
 */
final class SimulationExecutionService extends AbstractExecutionThreadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationExecutionService.class);
    private final Simulation<?> simulation;
    private final List<Predicate<Simulation<?>>> predicateList;

    @Inject
    private SimulationExecutionService(final SimulationModel<?> model,
                                     @Named("steps") final int steps) {
        this.predicateList = Lists.newArrayList();

        this.predicateList.add(new Predicate<Simulation<?>>() {
            @Override
            public boolean apply(final Simulation<?> simulation) {
                return simulation.getSteps() < steps;
            }
        });
        this.predicateList.add(new Predicate<Simulation<?>>() {
            @Override
            public boolean apply(@Nullable final Simulation<?> input) {
                return isRunning();
            }
        });

        LOGGER.info("Creating simulation for model {}", model.getClass());
        LOGGER.info("Created model {}", model);

        this.simulation = model.createSimulation();
        checkArgument(this.simulation != null, "createSimulation returned null for model {}", model);
    }

    @Override
    protected void run() throws Exception {
        Simulations.runWhile(simulation, Predicates.and(predicateList));
    }

    public Simulation<?> getSimulation() {
        return simulation;
    }
}
