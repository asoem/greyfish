package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.core.space.MovingObject2DInterface;
import org.asoem.greyfish.core.space.Object2DListener;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;

import java.awt.*;
import java.util.Iterator;

public class Agent extends GFAgentDecorator implements IndividualInterface, MovingObject2DInterface, Initializeable {

    private Agent(Agent individual, CloneMap map) {
        super(map.clone(individual.getDelegate(), IndividualInterface.class));
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

    private final Body body = Body.newInstance();

    private int id;

    public int getTimeOfBirth() {
        return timeOfBirth;
    }

    private int timeOfBirth;

    private GFAction lastExecutedAction;

    private Agent(IndividualInterface individual) {
        super(individual);
        freeze();
    }

    public static Agent newInstance(IndividualInterface individual) {
        return new Agent(individual);
    }

    public void setGenome(final Genome genome) {
        Preconditions.checkNotNull(genome);
        Iterator<Gene<?>> geneIterator = genome.iterator();

        for (GFProperty property : getProperties())
            property.setGenes(geneIterator);
    }

    @Override
    public void initialize(Simulation simulation) {
        Preconditions.checkNotNull(simulation);

        body.initialize(simulation);
        getGenome().initialize();

        // call initializers
        for (Initializeable component : getComponents()) {
            component.initialize(simulation);
        }
        for (Initializeable component : getComponents()) { // new sensors and actuators might have got instantiated after the first round // TODO Make this dirty hack unnecessary
            component.initialize(simulation);
        }
    }

    @Override
    public double getOrientation() {
        return body.getOrientation();
    }

    @Override
    public double getSpeed() {
        return body.getSpeed();
    }

    @Override
    public void rotate(double alpha) {
        body.rotate(alpha);
    }

    @Override
    public void setTimeOfBirth(int timeOfBirth) {
        this.timeOfBirth = timeOfBirth;
    }

    @Override
    public void execute(Simulation simulation) {
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
                toExecute.executeUnevaluated(simulation);
                lastExecutedAction = toExecute;

                if (GreyfishLogger.isDebugEnabled())
                    GreyfishLogger.debug("Executed " + toExecute + "@" + this.getId());
            }
        } catch (RuntimeException e) {
            GreyfishLogger.error("Error during execution of " + toExecute.getName(), e);
        }
    }

    @Override
    public Genome getGenome() {
        Genome.Builder builder = Genome.builder();
        for (GFProperty property : getProperties()) {
            builder.addAll(property.getGeneList());
        }
        return builder.build();
    }

    @Override
    public double getRadius() {
        return body.getRadius();
    }

    @Override
    public Color getColor() {
        return body.getColor();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Location2DInterface getAnchorPoint() {
        return body.getAnchorPoint();
    }

    @Override
    public void addListener(Object2DListener listener) {
        body.addListener(listener);
    }

    @Override
    public void removeListener(Object2DListener listener) {
        body.addListener(listener);
    }

    @Override
    public void setAnchorPoint(Location2DInterface location2d) {
        body.setAnchorPoint(location2d);
    }

    @Override
    public double getX() {
        return body.getX();
    }

    @Override
    public double getY() {
        return body.getY();
    }

    @Override
    public boolean isFrozen() {
        return true;
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new Agent(this, map);
    }

    public void setId(int id) {
        this.id = id;
    }
}
