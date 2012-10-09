package org.asoem.greyfish.core.agent;

import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Attribute;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.actions.utils.ActionState.PRECONDITIONS_MET;

public class ActiveSimulationContext implements SimulationContext {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(ActiveSimulationContext.class);

    //@Element(name = "simulation")
    private final Simulation simulation;

    @Attribute(name = "activationStep")
    private final int activationStep;

    @Attribute(name = "id")
    private final int id;

    @Nullable
    private AgentAction toResume;

    @Nullable
    private HistoryEntry historyEntry;

    private ActiveSimulationContext(Simulation simulation, int agentId, int simulationStep) {
        this.simulation = checkNotNull(simulation);
        this.id = agentId;
        this.activationStep = simulationStep;
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    private ActiveSimulationContext(@Attribute(name = "activationStep") int activationStep,
                                   @Attribute(name = "id") int id) {
        this.simulation = null;//checkNotNull(simulation);
        this.id = id;
        this.activationStep = activationStep;
    }

    public static ActiveSimulationContext create(Simulation simulation, int agentId, int simulationStep) {
        return new ActiveSimulationContext(simulation, agentId, simulationStep);
    }

    @Override
    public int getActivationStep() {
        return activationStep;
    }

    @Override
    @Nullable
    public AgentAction getLastExecutedAction() {
        return historyEntry == null ? null : historyEntry.action;
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
        assert simulation.getStep() >= activationStep;
        return simulation.getStep() - activationStep;
    }

    @Override
    public void execute(Agent agent) {

        assert historyEntry == null || historyEntry.step < simulation.getStep();

        AgentAction nextAction = null;

        // identify action to execute
        if (toResume != null) {
            nextAction = toResume;
        } else {
            for (AgentAction action : agent.getActions()) {
                if (action.checkPreconditions(simulation) == PRECONDITIONS_MET) {
                    nextAction = action;
                    break;
                } else
                    action.reset();
            }
        }

        // execute action
        if (nextAction != null) {

            LOGGER.info("{}: Executing {}", agent, nextAction);

            final ActionState state = nextAction.apply(simulation);

            LOGGER.debug("{}: Execution result {}", agent, state);

            switch (state) {

                case INTERMEDIATE:
                    toResume = nextAction;
                    return;

                case COMPLETED:
                    historyEntry = new HistoryEntry(simulation.getStep(), toResume);

                default:
                    nextAction.reset();
                    toResume = null;
                    return;
            }
        }

        LOGGER.debug("Could not execute anything for {}", agent);
    }

    @Override
    public void logEvent(Agent agent, Object eventOrigin, String title, String message) {
        checkNotNull(agent);
        checkNotNull(eventOrigin);
        checkNotNull(title);
        checkNotNull(message);

        final Object2D projection = agent.getProjection();
        assert projection != null;

        simulation.logAgentEvent(id, agent.getPopulation().getName(), projection.getAnchorPoint().getCoordinates(), eventOrigin, title, message);
    }

    @Override
    public int getSimulationStep() {
        return simulation.getStep();
    }

    @Override
    public boolean isActiveContext() {
        return true;
    }

    private static class HistoryEntry {
        private final int step;
        private final AgentAction action;

        private HistoryEntry(int step, AgentAction action) {
            this.step = step;
            this.action = action;
        }
    }
}