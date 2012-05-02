package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Attribute;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public class ActiveSimulationContext implements SimulationContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveSimulationContext.class);

    //@Element(name = "simulation")
    private final Simulation simulation;

    @Attribute(name = "activationStep")
    private final int activationStep;

    @Attribute(name = "id")
    private final int id;

    @Nullable
    private GFAction toResume;

    @Nullable
    private HistoryEntry historyEntry;

    public ActiveSimulationContext(Simulation simulation) {
        this.simulation = checkNotNull(simulation);
        this.id = simulation.generateAgentID();
        this.activationStep = simulation.getCurrentStep() + 1;
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for deserialization
    public ActiveSimulationContext(@Attribute(name = "activationStep") int activationStep,
                                   @Attribute(name = "id") int id) {
        this.simulation = null;//checkNotNull(simulation);
        this.id = id;
        this.activationStep = activationStep;
    }

    @Override
    public int getActivationStep() {
        return activationStep;
    }

    @Override
    @Nullable
    public GFAction getLastExecutedAction() {
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
        assert simulation.getCurrentStep() >= activationStep;
        return simulation.getCurrentStep() - activationStep;
    }

    @Override
    public void execute(Agent agent) {

        assert agent.getSimulationContext() == this : "Expected the agent associated with this context";
        assert historyEntry == null || historyEntry.step < simulation.getCurrentStep();

        GFAction nextAction = null;

        // identify action to execute
        if (toResume != null) {
            nextAction = toResume;
        }
        else {
            for (GFAction action : agent.getActions()) {

                assert action.getState() == ActionState.INITIAL :
                        "There should be no action in resuming state";

                if (action.checkPreconditions(simulation)) {
                    nextAction = action;
                    break;
                }
                else
                    action.reset();
            }
        }

        // execute action
        if (nextAction != null) {

            LOGGER.debug("{}#{}: Executing {}", agent, id, nextAction);

            final ActionState state = nextAction.apply(simulation);

            LOGGER.trace("{}#{}: Execution result {}", agent, id, state);

            switch (state) {

                case INTERMEDIATE:
                    toResume = nextAction;
                    return;

                case SUCCESS:
                    historyEntry = new HistoryEntry(simulation.getCurrentStep(), toResume);

                default:
                    nextAction.reset();
                    toResume = null;
                    return;
            }
        }

        LOGGER.debug("Could not execute anything for {}#{}", agent, id);
    }

    @Override
    public void logEvent(Agent agent, Object eventOrigin, String title, String message) {
        final Object2D projection = agent.getProjection();
        assert projection != null;
        simulation.createEvent(id, agent.getPopulation().getName(), projection.getCoordinates(), eventOrigin, title, message);
    }

    private static class HistoryEntry {
        private final int step;
        private final GFAction action;

        private HistoryEntry(int step, GFAction action) {
            this.step = step;
            this.action = action;
        }
    }
}