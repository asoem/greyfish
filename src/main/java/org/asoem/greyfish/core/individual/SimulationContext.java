package org.asoem.greyfish.core.individual;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.utils.ExecutionResult;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

class SimulationContext {
    private final Simulation simulation;
    private final int timeOfBirth;
    private final int id;
    private final Agent agent;
    private GFAction lastExecutedAction;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationContext.class);

    public SimulationContext(Simulation simulation, Agent agent) {
        this.simulation = checkNotNull(simulation);
        this.agent = checkNotNull(agent);
        this.id = simulation.generateAgentID();
        this.timeOfBirth = simulation.getSteps();
    }

    private SimulationContext() {
        simulation = null;
        timeOfBirth = 0;
        id = 0;
        agent = null;
    }

    public int getTimeOfBirth() {
        return timeOfBirth;
    }

    public GFAction getLastExecutedAction() {
        return lastExecutedAction;
    }

    public int getId() {
        return id;
    }

    public Simulation getSimulation() {
        return simulation;
    }

    public int getAge() {
        assert simulation.getSteps() >= timeOfBirth;
        return simulation.getSteps() - timeOfBirth;
    }

    public void execute() {
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

    static final SimulationContext NULL_CONTEXT = new SimulationContext() {
        @Override
        public int getTimeOfBirth() {
            throw new UnsupportedOperationException();
        }

        @Override
        public GFAction getLastExecutedAction() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Simulation getSimulation() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getAge() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void execute() {
            throw new UnsupportedOperationException();
        }
    };
}