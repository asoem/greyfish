package org.asoem.greyfish.core.individual;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.actions.utils.ExecutionResult;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class SimulationContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationContext.class);

    private final Simulation simulation;
    private final int timeOfBirth;
    private final int id;

    @Nullable
    private GFAction lastExecutedAction;

    public SimulationContext(Simulation simulation, Agent agent) {
        this.simulation = checkNotNull(simulation);
        this.id = simulation.generateAgentID();
        this.timeOfBirth = simulation.getSteps();
    }

    private SimulationContext() {
        simulation = null;
        timeOfBirth = 0;
        id = 0;
    }

    public int getTimeOfBirth() {
        return timeOfBirth;
    }

    @Nullable
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimulationContext that = (SimulationContext) o;

        if (id != that.id) return false;
        if (timeOfBirth != that.timeOfBirth) return false;
        if (lastExecutedAction != null ? !lastExecutedAction.equals(that.lastExecutedAction) : that.lastExecutedAction != null)
            return false;
        if (simulation != null ? !simulation.equals(that.simulation) : that.simulation != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = simulation != null ? simulation.hashCode() : 0;
        result = 31 * result + timeOfBirth;
        result = 31 * result + id;
        result = 31 * result + (lastExecutedAction != null ? lastExecutedAction.hashCode() : 0);
        return result;
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
        public void execute(Agent agent) {
            throw new UnsupportedOperationException();
        }
    };
}