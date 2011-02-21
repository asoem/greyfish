package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.core.space.Object2DInterface;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;

import java.awt.*;
import java.util.Iterator;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.io.GreyfishLogger.*;

public class Agent extends GFAgentDecorator implements IndividualInterface, Object2DInterface, Initializeable {

    // static during each activation
    private final Simulation simulation;
    private final int timeOfBirth;
    private final int id;

    // dynamic during each activation
    private GFAction lastExecutedAction;

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
        Iterator<Gene<?>> geneIterator = genome.iterator();

        for (GFProperty property : getProperties())
            property.setGenes(geneIterator);
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
        GFAction toExecute = lastExecutedAction;

        if (toExecute == null
                || lastExecutedAction.done()) {
            toExecute = null;
            for (GFAction action : getActions()) {
                if (action.evaluate(simulation)) {
                    toExecute = action;
                    break;
                }
            }
        }
        try {
            if (toExecute != null) {
                if (isTraceEnabled()) trace("Executing " + toExecute);
                toExecute.executeUnevaluated(simulation);
                lastExecutedAction = toExecute;
            } else {
                if (isTraceEnabled()) trace("Found no Action to execute.");
            }
        } catch (RuntimeException e) {
            error("Error during execution of " + toExecute + " for " + this, e);
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
    public Location2DInterface getAnchorPoint() {
        return getBody().getAnchorPoint();
    }

    @Override
    public void setAnchorPoint(Location2DInterface location2d) {
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
        return "Agent#" + id + "(" + getPopulation() + ")[" + getGenome() + "]";
    }

    public Individual getIndividual() {
        return Individual.class.cast(getDelegate());
    }

    public Simulation getSimulation() {
        return simulation;
    }

    @Override
    public float getAge() {
        return simulation.getSteps() - getTimeOfBirth();
    }
}
