package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.genes.GenomeInterface;
import org.asoem.greyfish.core.interfaces.GFInterface;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.core.space.Object2DListener;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.CloneMap;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class GFAgentDecorator extends AbstractDeepCloneable implements IndividualInterface {

    private final IndividualInterface delegate;

    protected GFAgentDecorator(IndividualInterface delegate, CloneMap map) {
        super(delegate, map);
        this.delegate = map.clone(delegate, IndividualInterface.class);
    }

    protected IndividualInterface getDelegate() {
        return delegate;
    }

    protected GFAgentDecorator(IndividualInterface individualInterface) {
        this.delegate = individualInterface;
    }

    @Override
    public Population getPopulation() {
        return delegate.getPopulation();
    }

    @Override
    public void setPopulation(Population population) {
        delegate.setPopulation(population);
    }

    @Override
    public boolean addAction(GFAction action) {
        return delegate.addAction(action);
    }

    @Override
    public boolean hasAction(String name) {
        return delegate.hasAction(name);
    }

    @Override
    public boolean removeAction(GFAction action) {
        return delegate.removeAction(action);
    }

    @Override
    public void removeAllActions() {
        delegate.removeAllActions();
    }

    @Override
    public List<GFAction> getActions() {
        return delegate.getActions();
    }

    @Override
    public <T extends GFAction> T getAction(Class<T> t, String actionName) {
        return delegate.getAction(t, actionName);
    }

    @Override
    public <T extends GFAction> Iterable<GFAction> getActions(Class<T> class1) {
        return delegate.getActions(class1);
    }

    @Override
    public boolean addProperty(GFProperty property) {
        return delegate.addProperty(property);
    }

    @Override
    public boolean hasProperty(String name) {
        return delegate.hasProperty(name);
    }

    @Override
    public boolean removeProperty(GFProperty property) {
        return delegate.removeProperty(property);
    }

    @Override
    public void removeAllProperties() {
        delegate.removeAllProperties();
    }

    @Override
    public List<GFProperty> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public <T extends GFProperty> Iterable<T> getProperties(Class<T> clazz) {
        return delegate.getProperties(clazz);
    }

    @Override
    public boolean isCloneOf(Object object) {
        return delegate.isCloneOf(object);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Iterable<? extends GFComponent> getComponents() {
        return delegate.getComponents();
    }

    @Override
    public <T extends GFInterface> T getInterface(Class<T> clazz) throws NoSuchElementException {
        return delegate.getInterface(clazz);
    }

    @Override
    public void changeActionExecutionOrder(GFAction object, GFAction object2) {
        delegate.changeActionExecutionOrder(object, object2);
    }

    @Override
    public int getId() {
        return delegate.getId();
    }

    @Override
    public void setId(int i) {
        delegate.setId(i);
    }

    @Override
    public void setTimeOfBirth(int timeOfBirth) {
        delegate.setTimeOfBirth(timeOfBirth);
    }

    @Override
    public int getTimeOfBirth() {
        return delegate.getTimeOfBirth();
    }

    @Override
    public Color getColor() {
        return delegate.getColor();
    }

    @Override
    public void setColor(Color color) {
        delegate.setColor(color);
    }

    @Override
    public double getRadius() {
        return delegate.getRadius();
    }

    @Override
    public GenomeInterface getGenome() {
        return delegate.getGenome();
    }

    @Override
    public void setGenome(Genome genome) {
        delegate.setGenome(genome);
    }

    @Override
    public GFAction getLastExecutedAction() {
        return delegate.getLastExecutedAction();
    }

    @Override
    public void freeze() {
        delegate.freeze();
    }

    @Override
    public boolean isFrozen() {
        return delegate.isFrozen();
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) throws IllegalStateException {
        delegate.checkConsistency(components);
    }

    @Override
    public <T> T checkFrozen(T value) throws IllegalStateException {
        return delegate.checkFrozen(value);
    }

    @Override
    public void checkNotFrozen() throws IllegalStateException {
        delegate.checkNotFrozen();
    }

    @Override
    public Iterator<GFComponent> iterator() {
        return delegate.iterator();
    }

    @Override
    public double getOrientation() {
        return delegate.getOrientation();
    }

    @Override
    public double getSpeed() {
        return delegate.getSpeed();
    }

    @Override
    public void rotate(double alpha) {
        delegate.rotate(alpha);
    }

    @Override
    public Location2DInterface getAnchorPoint() {
        return delegate.getAnchorPoint();
    }

    @Override
    public void setAnchorPoint(Location2DInterface location2d) {
        delegate.setAnchorPoint(location2d);
    }

    @Override
    public void addListener(Object2DListener listener) {
        delegate.addListener(listener);
    }

    @Override
    public void removeListener(Object2DListener listener) {
        delegate.removeListener(listener);
    }

    @Override
    public double getX() {
        return delegate.getX();
    }

    @Override
    public double getY() {
        return delegate.getY();
    }

    @Override
    public void execute(Simulation simulation) {
        delegate.execute(simulation);
    }
}
