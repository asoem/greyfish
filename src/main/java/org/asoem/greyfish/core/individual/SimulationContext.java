package org.asoem.greyfish.core.individual;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AbstractGFAction;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.lang.CircularFifoBuffer;

import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

class SimulationContext {
    private final CircularFifoBuffer<ACLMessage> inBox;
    private final ParallelizedSimulation simulation;
    private final int timeOfBirth;
    private final int id;
    private final Agent agent;
    private GFAction lastExecutedAction;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationContext.class);

    public SimulationContext(ParallelizedSimulation simulation, Agent agent) {
        this.simulation = checkNotNull(simulation);
        this.agent = checkNotNull(agent);
        this.id = simulation.generateAgentID();
        this.timeOfBirth = simulation.getSteps();
        this.inBox = CircularFifoBuffer.newInstance(64);
    }

    private SimulationContext() {
        inBox = null;
        simulation = null;
        timeOfBirth = 0;
        id = 0;
        agent = null;
    }

    public void pushMessages(Iterable<? extends ACLMessage> messages) {
        Iterables.addAll(inBox, messages);
    }

    public void pushMessage(ACLMessage message) {
        inBox.add(message);
    }

    public List<ACLMessage> pullMessages(MessageTemplate template) {
        checkNotNull(template);
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
        return Iterables.any(inBox, checkNotNull(template));
    }

    public int getTimeOfBirth() {
        return timeOfBirth;
    }

    public void sendMessage(ACLMessage message) {
        simulation.deliverMessage(checkNotNull(message));
    }

    public GFAction getLastExecutedAction() {
        return lastExecutedAction;
    }

    public int getId() {
        return id;
    }

    public ParallelizedSimulation getSimulation() {
        return simulation;
    }

    public int getAge() {
        assert simulation.getSteps() >= timeOfBirth;
        return simulation.getSteps() - timeOfBirth;
    }

    public Iterable<MovingObject2D> findNeighbours(double range) {
        return simulation.findObjects(agent.getCoordinates(), range);
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

    static SimulationContext NULL_CONTEXT = new SimulationContext() {
        @Override
        public void pushMessages(Iterable<? extends ACLMessage> messages) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void pushMessage(ACLMessage message) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<ACLMessage> pullMessages(MessageTemplate template) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasMessages(MessageTemplate template) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getTimeOfBirth() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendMessage(ACLMessage message) {
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
        public ParallelizedSimulation getSimulation() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getAge() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterable<MovingObject2D> findNeighbours(double range) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void execute() {
            throw new UnsupportedOperationException();
        }
    };
}