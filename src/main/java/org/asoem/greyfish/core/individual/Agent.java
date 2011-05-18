package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.AbstractGFAction;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.io.*;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.Preparable;

import java.awt.*;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class Agent extends GFAgentDecorator implements IndividualInterface, MovingObject2D, Preparable<Simulation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Agent.class);

    private final Simulation simulation;
    private final int timeOfBirth;
    private final int id;

    private final static AgentLogFactory AGENT_LOG_FACTORY = new DefaultAgentLogFactory("agents.log");
    private final AgentLog log = AGENT_LOG_FACTORY.newAgentLog();

    private GFAction lastExecutedAction;

    private Agent(Agent individual, CloneMap map) {
        super(map.clone(individual.getDelegate(), IndividualInterface.class));

        this.simulation = checkNotNull(individual.simulation);
        this.id = simulation.generateAgentID();
        this.timeOfBirth = simulation.getSteps();

        init();
    }

    private Agent(IndividualInterface individual, Simulation simulation) {
        super(individual);

        this.simulation = checkNotNull(simulation);
        this.id = simulation.generateAgentID();
        this.timeOfBirth = simulation.getSteps();

        init();
    }

    private void init() {
        prepare(simulation);
        freeze();

        // logging
        log.set("id", id);
        log.set("timeOfBirth", timeOfBirth);

        int i=0;
        for (Gene<?> gene : getGenome())
            log.set("Gene#" + String.valueOf(i++), gene);
    }

    @Override
    public void setPopulation(Population population) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAction(GFAction action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAction(GFAction action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllActions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addProperty(GFProperty property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeProperty(GFProperty property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void changeActionExecutionOrder(GFAction object, GFAction object2) {
        throw new UnsupportedOperationException();
    }

    public int getTimeOfBirth() {
        return timeOfBirth;
    }

    public static Agent newInstance(Individual individual, Simulation simulation) {
        return new Agent(individual, simulation);
    }

    public void setGenome(final Genome genome) {
        Preconditions.checkNotNull(genome);

        for (GFProperty property : getProperties())
            property.setGenes(genome.getGenes());
    }

    @Override
    public void prepare(Simulation simulation) {
        // call prepare for all components
        for (GFComponent component : this) {
            component.setComponentRoot(this);
            component.prepare(simulation);
        }
    }
    // TODO move to an interface?
    public void shutDown() {
        log.set("timeOfDeath", simulation.getSteps());

        try {
            log.commit();
        } catch (IOException e) {
            LOGGER.error("Log could not be committed: {}", log, e);
        }
    }

    @Override
    public double getOrientation() {
        return getBody().getOrientation();
    }

    @Override
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

        LOGGER.trace("{}: Processing " + Iterables.size(getActions()) + " actions in order", this);

        for (GFAction action : getActions()) {
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

    @Override
    public Genome getGenome() {
        Genome.Builder builder = Genome.builder();
        for (GFProperty property : getProperties()) {
            builder.addAll(property.getGenes());
        }
        return builder.build();
    }

    @Override
    public double getRadius() {
        return getBody().getRadius();
    }

    @Override
    public GFAction getLastExecutedAction() {
        return lastExecutedAction;
    }

    @Override
    public Color getColor() {
        return getBody().getColor();
    }

    @Override
    public void setColor(Color color) {
        getBody().setColor(color);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Location2D getAnchorPoint() {
        return getBody().getAnchorPoint();
    }

    @Override
    public void setAnchorPoint(Location2D location2d) {
        getBody().setAnchorPoint(location2d);
    }

    @Override
    public double getX() {
        return getBody().getX();
    }

    @Override
    public double getY() {
        return getBody().getY();
    }

    @Override
    public void freeze() {
        for (GFComponent component : getComponents())
            component.freeze();
    }

    @Override
    public boolean isFrozen() {
        return true;
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) throws IllegalStateException {
        for (GFComponent component : getComponents())
            component.checkConsistency(getComponents());
    }

    @Override
    public <T> T checkFrozen(T value) throws IllegalStateException {
        return value;
    }

    @Override
    public void checkNotFrozen() throws IllegalStateException {
        throw new IllegalStateException("Agents are always in frozen state");
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new Agent(this, map);
    }

    @Override
    public void setOrientation(double alpha) {
        getBody().setOrientation(alpha);
    }

    @Override
    public String toString() {
        return getPopulation() + "#" + id;
    }

    public Individual getIndividual() {
        return Individual.class.cast(getDelegate());
    }

    public Simulation getSimulation() {
        return simulation;
    }

    @Override
    public int getAge() {
        assert simulation.getSteps() >= getTimeOfBirth();
        return simulation.getSteps() - getTimeOfBirth();
    }

    @Override
    public AgentLog getLog() {
        return log;
    }
}
