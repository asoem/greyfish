package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import javolution.util.FastList;
import org.asoem.greyfish.core.actions.AbstractGFAction;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;

import java.awt.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.io.GreyfishLogger.AGENT_LOGGER;

public class Agent extends GFAgentDecorator implements IndividualInterface, MovingObject2D, Initializeable {

    // static during each activation
    private final Simulation simulation;
    private final int timeOfBirth;
    private final int id;

    // dynamic during each activation
    private GFAction lastExecutedAction;

    private FastList<GFAction> resumeQueue = FastList.newInstance();

    private Agent(Agent individual, CloneMap map) {
        super(map.clone(individual.getDelegate(), IndividualInterface.class));

        this.simulation = checkNotNull(individual.simulation);
        this.id = simulation.generateAgentID();
        this.timeOfBirth = simulation.getSteps();

        initialize(simulation);
        freeze();
    }

    private Agent(IndividualInterface individual, Simulation simulation) {
        super(individual);

        this.simulation = checkNotNull(simulation);
        this.id = simulation.generateAgentID();
        this.timeOfBirth = simulation.getSteps();

        initialize(simulation);
        freeze();
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
    public void initialize(Simulation simulation) {
        // call initialize for all components
        for (GFComponent component : this) {
            component.setComponentRoot(this);
            component.initialize(simulation);
        }
    }

    @Override
    public double getOrientation() {
        return getBody().getOrientation();
    }

    @Override
    public void execute() {
        if (lastExecutedAction != null &&
                lastExecutedAction.isResuming()) {
            if (AGENT_LOGGER.isDebugEnabled())
                AGENT_LOGGER.debug(id + ": Resuming " + lastExecutedAction);
            if (executeAction(lastExecutedAction)) {
                return;
            } else {
                GreyfishLogger.AGENT_LOGGER.error("Resume failed");
            }
        }
        else {
            if (AGENT_LOGGER.isDebugEnabled())
                AGENT_LOGGER.trace(id + ": Processing " + Iterables.size(getActions()) + " actions in order");
            for (GFAction action : getActions()) {
                assert !action.isResuming();

                if (executeAction(action)) {
                    lastExecutedAction = action;
                    return;
                }
            }
        }

        if (AGENT_LOGGER.isDebugEnabled())
            AGENT_LOGGER.debug("Nothing to execute");
    }

    private boolean executeAction(GFAction action) {
        StringBuffer debugString = null;

        if (AGENT_LOGGER.isDebugEnabled()) {
            debugString = new StringBuffer("Agent#");
            debugString.append(id).append(": Trying to execute ").append(action).append(": ");
        }

        final AbstractGFAction.ExecutionResult result = action.execute(simulation);

        switch (result) {
            case CONDITIONS_FAILED:
                if (AGENT_LOGGER.isDebugEnabled())
                    AGENT_LOGGER.debug(debugString.append("FAILED: Attached conditions evaluated to false.").toString());
                return false;
            case INVALID_INTERNAL_STATE:
                if (AGENT_LOGGER.isDebugEnabled())
                    AGENT_LOGGER.debug(debugString.append("FAILED: Internal preconditions evaluated to false.").toString());
                return false;
            case INSUFFICIENT_ENERGY:
                if (AGENT_LOGGER.isDebugEnabled())
                    AGENT_LOGGER.debug(debugString.append("FAILED: Not enough energy.").toString());
                return false;
            case ERROR:
                if (AGENT_LOGGER.isDebugEnabled())
                    AGENT_LOGGER.debug(debugString.append("FAILED: Internal error.").toString());
                return false;
            case EXECUTED:
                if (AGENT_LOGGER.isDebugEnabled())
                    AGENT_LOGGER.debug(debugString.append("SUCCESS").toString());
                return true;
            default: // should never be reached
                AGENT_LOGGER.debug(debugString.append("ERROR: action returned unhandled state ").append(result).append('.').toString());
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
        return "Agent#" + id + "('" + getPopulation() + "'@" + getGenome() + ")";
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
}
