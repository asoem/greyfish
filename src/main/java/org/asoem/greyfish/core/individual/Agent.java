package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.interfaces.GFInterface;
import org.asoem.greyfish.core.io.GreyfishLogger;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Initializeable;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.core.space.MovingObject2DInterface;
import org.asoem.greyfish.core.space.Object2DListener;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Agent implements IndividualInterface, MovingObject2DInterface, SimulationObject {

    @Override
    public Iterator<GFComponent> iterator() {
        return Iterators.unmodifiableIterator(individual.iterator());
    }

    @Override
    public Population getPopulation() {
        return individual.getPopulation();
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
    public List<GFAction> getActions() {
        return individual.getActions();
    }

    @Override
    public <T extends GFAction> T getAction(Class<T> t, String actionName) {
        return individual.getAction(t, actionName);
    }

    @Override
    public <T extends GFAction> Iterable<GFAction> getActions(Class<T> class1) {
        return individual.getActions(class1);
    }

    @Override
    public boolean addProperty(GFProperty property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasProperty(String name) {
        return individual.hasProperty(name);
    }

    @Override
    public boolean hasAction(String name) {
        return individual.hasAction(name);
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
    public List<GFProperty> getProperties() {
        return individual.getProperties();
    }

    @Override
    public <T extends GFProperty> Iterable<T> getProperties(Class<T> clazz) {
        return individual.getProperties(clazz);
    }

    @Override
    public String toString() {
        return individual.toString();
    }

    @Override
    public boolean isCloneOf(Object object) {
        return individual.isCloneOf(object);
    }

    @Override
    public String getName() {
        return individual.getName();
    }

    @Override
    public Iterable<? extends GFComponent> getComponents() {
        return individual.getComponents();
    }

    @Override
    public <T extends GFInterface> T getInterface(Class<T> clazz) throws NoSuchElementException {
        return individual.getInterface(clazz);
    }

    @Override
    public void changeActionExecutionOrder(GFAction object, GFAction object2) {
        throw new UnsupportedOperationException();
    }

    public void componentRemoved(GFComponent component) {
        individual.componentRemoved(component);
    }

    @Override
    public void freeze() {
        individual.freeze();
    }

    private final Individual individual;

    private Genome genome;

    private Body body;

    private final int id;

    public int getTimeOfBirth() {
        return timeOfBirth;
    }

    private int timeOfBirth;

    private GFAction lastExecutedAction;

    private Agent(Individual individual, int id) {
        this.individual = individual;
        this.id = id;
        freeze();
    }

    public static Agent newInstance(Individual individual, int id) {
        return new Agent(individual, id);
    }

    private void finishAssembly() {
        assembleGenome();
    }

    public void mutate() {
        genome.mutate();
    }

    public void setGenome(final Genome genome) {
        assert this.genome != null;
        Preconditions.checkNotNull(genome);
        this.genome.initGenome(genome);
    }

    @Override
    public void initialize(Simulation simulation) {
        Preconditions.checkNotNull(simulation);

        body.initialize(simulation);
        genome.initialize();

        // call initializers
        for (Initializeable component : getComponents()) {
            component.initialize(simulation);
        }
        for (Initializeable component : getComponents()) { // new sensors and actuators might have got instantiated after the first round //TODO Make this dirty hack unnecessary
            component.initialize(simulation);
        }
    }

    /**
     * Assemble the genome from the current set of the individual's properties.
     * This means, that the genome is not updated automatically.
     */
    private void assembleGenome() {
        assert genome != null;
        assert getProperties() != null;
        genome.clear();
        for (GFProperty property : getProperties()) {
            genome.addAll(property.getGeneList());
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

    /**
     * @param simulation The simulation context
     * @return a deepClone of this individual fetched from the simulations pool of clones with the identical genetic constitution
     */
    public Agent createClone(Simulation simulation) {
        Preconditions.checkNotNull(simulation);
        final Agent ret = simulation.createClone(getPopulation());
        ret.setGenome(new Genome(genome));
        return ret;
    }

    @Override
    public Genome getGenome() {
        return genome;
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
    public Location2D getAnchorPoint() {
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
    public void checkConsistency(Iterable<? extends GFComponent> components) throws IllegalStateException {
        individual.checkConsistency(components);
    }

    @Override
    public <T> T checkFrozen(T value) {
        return individual.checkFrozen(value);
    }

    @Override
    public void checkNotFrozen() {
        individual.checkNotFrozen();
    }

    @Override
    public Individual deepClone() {
        return individual.deepClone();
    }
}
