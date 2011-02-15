package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.DeepCloneable;
import org.simpleframework.xml.Element;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

public abstract class GFAgentDecorator extends AbstractDeepCloneable implements IndividualInterface {

    @Element(name="delegate")
    private final IndividualInterface delegate;

    protected GFAgentDecorator(IndividualInterface delegate) {
        this.delegate = delegate;
    }

    protected IndividualInterface getDelegate() {
        return delegate;
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
    public boolean removeAction(GFAction action) {
        return delegate.removeAction(action);
    }

    @Override
    public void removeAllActions() {
        delegate.removeAllActions();
    }

    @Override
    public Iterable<GFAction> getActions() {
        return delegate.getActions();
    }

    @Override
    public boolean addProperty(GFProperty property) {
        return delegate.addProperty(property);
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
    public Iterable<GFProperty> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public boolean isCloneOf(Object object) {
        return delegate.isCloneOf(object);
    }

    @Override
    public Iterable<GFComponent> getComponents() {
        return delegate.getComponents();
    }

    @Override
    public void changeActionExecutionOrder(GFAction object, GFAction object2) {
        delegate.changeActionExecutionOrder(object, object2);
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return delegate.deepCloneHelper(map);
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
    public double getY() {
        return delegate.getY();
    }

    @Override
    public double getX() {
        return delegate.getX();
    }

    @Override
    public void setAnchorPoint(Location2DInterface location2d) {
        delegate.setAnchorPoint(location2d);
    }

    @Override
    public Location2DInterface getAnchorPoint() {
        return delegate.getAnchorPoint();
    }

    @Override
    public double getOrientation() {
        return delegate.getOrientation();
    }

    @Override
    public int getId() {
        return delegate.getId();
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
    public Genome getGenome() {
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
    public Body getBody() {
        return delegate.getBody();
    }

    @Override
    public List pollMessages(MessageTemplate template) {
        return delegate.pollMessages(template);
    }

    @Override
    public void addMessages(Iterable<? extends ACLMessage> messages) {
        delegate.addMessages(messages);
    }

    @Override
    public float getAge() {
        return delegate.getAge();
    }
}
