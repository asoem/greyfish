package org.asoem.greyfish.core.individual;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AbstractGFAction;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.lang.CircularFifoBuffer;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class SimulationContext {
    private final CircularFifoBuffer<ACLMessage> inBox = CircularFifoBuffer.newInstance(64);
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

    public void pushMessages(Iterable<? extends ACLMessage> messages) {
        Iterables.addAll(inBox, messages);
    }

    public void pushMessage(ACLMessage message) {
        inBox.add(message);
    }

    public List<ACLMessage> pullMessages(MessageTemplate template) {
        List<ACLMessage> ret = Lists.newArrayList();
        Iterator<ACLMessage> iterator = inBox.listIterator();
        while (iterator.hasNext()) {
            ACLMessage message = iterator.next();
            if (template.apply(message))
                ret.add(message);
            iterator.remove();
        }
        return ret;
    }

    public boolean hasMessages(MessageTemplate template) {
        return Iterables.any(inBox, template);
    }

    public int getTimeOfBirth() {
        return timeOfBirth;
    }

    public void sendMessage(ACLMessage message) {
        simulation.deliverMessage(message);
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

    public Iterable<MovingObject2D> findNeighbours(double range) {
        return simulation.findObjects(agent, range);
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

        LOGGER.trace("{}: Trying to execute {}", this, action);

        final AbstractGFAction.ExecutionResult result = action.execute(simulation);

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