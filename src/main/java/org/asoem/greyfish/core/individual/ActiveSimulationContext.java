package org.asoem.greyfish.core.individual;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.actions.utils.ExecutionResult;
import org.asoem.greyfish.core.io.AgentEvent;
import org.asoem.greyfish.core.io.SimulationLogger;
import org.asoem.greyfish.core.io.SimulationLoggerProvider;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class ActiveSimulationContext implements SimulationContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveSimulationContext.class);

    @Element(name = "simulation")
    private final Simulation simulation;

    @Attribute(name = "firstStep")
    private final int firstStep;

    @Attribute(name = "id")
    private final int id;

    @Nullable
    private GFAction lastExecutedAction;

    private final SimulationLogger simulationLogger;

    public ActiveSimulationContext(Simulation simulation) {
        this.simulation = checkNotNull(simulation);
        this.id = simulation.generateAgentID();
        this.firstStep = simulation.getSteps() + 1;
        this.simulationLogger = SimulationLoggerProvider.getLogger(simulation);
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    public ActiveSimulationContext(@Element(name = "simulation") Simulation simulation, @Attribute(name = "firstStep") int firstStep, @Attribute(name = "id") int id) {
        this.simulation = checkNotNull(simulation);
        this.id = id;
        this.firstStep = firstStep;
        this.simulationLogger = SimulationLoggerProvider.getLogger(simulation);
    }

    @Override
    public int getFirstStep() {
        return firstStep;
    }

    @Override
    @Nullable
    public GFAction getLastExecutedAction() {
        return lastExecutedAction;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Simulation getSimulation() {
        return simulation;
    }

    @Override
    public int getAge() {
        assert simulation.getSteps() >= firstStep;
        return simulation.getSteps() - firstStep;
    }

    @Override
    public void execute(Agent agent) {
        if (lastExecutedAction != null &&
                !lastExecutedAction.isDormant()) {
            LOGGER.debug("{}: Resuming {}", this, lastExecutedAction);
            if (tryToExecute(lastExecutedAction)) {
                return;
            } else {
                LOGGER.debug("{}: Resume failed", this);
                lastExecutedAction = null;
                // TODO: should the method return here?
            }
        }

        LOGGER.trace("{}: Processing " + Iterables.size(agent.getActions()) + " actions in order", this);

        for (GFAction action : agent.getActions()) {
            assert action.isDormant() : "There should be no action in resuming state";

            if (tryToExecute(action)) {
                LOGGER.debug("{}: Executed {}", this, action);
                lastExecutedAction = action;
                return;
            }
        }

        LOGGER.trace("{}: Nothing to execute", this);
    }

    @Override
    public void logEvent(AgentEvent event) {
        simulationLogger.addEvent(event);
        LOGGER.debug("Event sent to logger: {}", event);
    }

    private boolean tryToExecute(GFAction action) {
        assert action != null;

        LOGGER.trace("{}: Trying to execute {}", this, action);

        final ExecutionResult result = action.execute(simulation);

        switch (result) {
            case CONDITIONS_FAILED:
                LOGGER.trace("FAILED: Attached conditions evaluated to false.");
                return false;
            case INSUFFICIENT_ENERGY:
                LOGGER.trace("FAILED: Not enough energy.");
                return false;
            case ERROR:
                LOGGER.trace("FAILED: Internal error.");
                return false;
            case EXECUTED:
                LOGGER.trace("SUCCESS");
                return true;
            case FAILED:
                LOGGER.trace("SUCCESS");
                return true;
            default:
                assert false : "Code should never be reached";
                return false;
        }
    }
}