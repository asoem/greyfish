package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Coordinates2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.lang.TreeNode;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.PolarPoint;
import org.simpleframework.xml.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AgentDecorator implements Agent {

    @Element(name="delegate")
    private final Agent delegate;

    /**
     * After decoration, the {@link AgentComponent#getAgent()} method of all {@link AgentComponent}s of {@code delegate}
     * will return this {@code AgentDecorator} instead of {@code delegate}.
     * @param delegate the {@code Agent} to decorate
     */
    protected AgentDecorator(@Nonnull Agent delegate) {
        this.delegate = checkNotNull(delegate);
        /*
        for (AgentComponent component : delegate.getComponents()) {

            component.setAgent(this);
        }
        */
    }

    protected Agent delegate() {
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

    /**
     * If you wish overwrite {@link Agent#addAction}, make sure to call {@link AgentComponent#setAgent} on {@code action} after addition.
     */
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

    /**
     * If you wish overwrite {@link Agent#addProperty}, make sure to call {@link AgentComponent#setAgent} on {@code action} after addition.
     */
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
    @Nullable
    public <T extends GFProperty> T getProperty(String name, Class<T> propertyClass) {
        return delegate.getProperty(name, propertyClass);
    }

    @Override
    public boolean isCloneOf(Object object) {
        return delegate.isCloneOf(object);
    }

    @Override
    public Iterable<AgentComponent> getComponents() {
        return delegate.getComponents();
    }

    @Override
    public void changeActionExecutionOrder(GFAction object, GFAction object2) {
        delegate.changeActionExecutionOrder(object, object2);
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return delegate.deepClone(cloner);
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
    public void checkNotFrozen() throws IllegalStateException {
        delegate.checkNotFrozen();
    }

    @Override
    public Iterator<AgentComponent> iterator() {
        return delegate.iterator();
    }

    @Override
    public void setAnchorPoint(Coordinates2D coordinates2d) {
        delegate.setAnchorPoint(coordinates2d);
    }

    @Override
    public Coordinates2D getCoordinates() {
        return delegate.getCoordinates();
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
    public Genome createGamete() {
        return delegate.createGamete();
    }

    @Override
    public void injectGamete(Genome genome) {
        delegate.injectGamete(genome);
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
    public List<ACLMessage> pullMessages(MessageTemplate template) {
        return delegate.pullMessages(template);
    }

    @Override
    public boolean hasMessages(MessageTemplate template) {
        return delegate.hasMessages(template);
    }

    @Override
    public void pushMessages(Iterable<? extends ACLMessage> messages) {
        delegate.pushMessages(messages);
    }

    @Override
    public void pushMessage(ACLMessage messages) {
        delegate.pushMessage(messages);
    }

    @Override
    public int getAge() {
        return delegate.getAge();
    }

    @Override
    public PolarPoint getMotionVector() {
        return delegate.getMotionVector();
    }

    @Override
    public void setMotionVector(PolarPoint polarPoint) {
        delegate.setMotionVector(polarPoint);
    }

    @Override
    public void changeMotion(double angle, double velocity) {
        delegate.changeMotion(angle, velocity);
    }

    @Override
    public void setMotion(double angle, double velocity) {
        delegate.setMotion(angle, velocity);
    }

    @Override
    public void execute() {
        delegate.execute();
    }

    @Override
    public void sendMessage(ACLMessage message) {
        delegate.sendMessage(message);
    }

    @Override
    public Iterable<MovingObject2D> findNeighbours(double range) {
        return delegate.findNeighbours(range);
    }

    @Override
    public void shutDown() {
        delegate.shutDown();
    }

    @Override
    public void setOrientation(double alpha) {
        delegate.setOrientation(alpha);
    }

    @Override
    public Simulation getSimulation() {
        return delegate.getSimulation();
    }

    @Override
    public void setSimulation(Simulation simulation) {
        delegate.setSimulation(simulation);
    }

    @Override
    public void prepare(Simulation context) {
        delegate.prepare(context);
    }

    @Override
    public <T extends GFAction> T getAction(String actionName, Class<T> gfActionClass) {
        return delegate.getAction(actionName, gfActionClass);
    }

    @Override
    public <T extends Gene> T getGene(String geneName, Class<T> geneClass) {
        return delegate.getGene(geneName, geneClass);
    }

    @Override
    public boolean addGene(Gene<?> gene) {
        return delegate.addGene(gene);
    }

    @Override
    public boolean removeGene(Gene<?> gene) {
        return delegate.removeGene(gene);
    }

    @Override
    public void removeAllGenes() {
        delegate.removeAllGenes();
    }

    @Override
    public Iterable<Gene<?>> getGenes() {
        return delegate.getGenes();
    }

    @Override
    public TreeNode<AgentComponent> getRootComponent() {
        return delegate.getRootComponent();
    }
}
