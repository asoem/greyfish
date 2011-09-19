package org.asoem.greyfish.core.individual;

import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.io.AgentLog;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.PolarPoint;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 16:20
 */
public abstract class AbstractAgent extends AbstractDeepCloneable implements Agent {

    @ElementList(name="properties", entry="property", required=false)
    protected final ComponentList<GFProperty> properties;
    
    @ElementList(name="actions", entry="action", required=false)
    protected final ComponentList<GFAction> actions;
    
    @Element(name="genome", required=false)
    protected final Genome genome;
    
    @Element(name = "body", required = false)
    protected final Body body;
    
    protected Population population;

    @SimpleXMLConstructor
    protected AbstractAgent(@Element(name = "body", required = false) Body body,
                            @ElementList(name="properties", entry="property", required=false) ComponentList<GFProperty> properties,
                            @ElementList(name="actions", entry="action", required=false) ComponentList<GFAction> actions,
                            @Element(name="genome", required=false) Genome genome) {
        this.body = body;
        this.properties = properties;
        this.actions = actions;
        this.genome = genome;
    }
    
    @SuppressWarnings("unchecked")
    protected AbstractAgent(AbstractAgent abstractAgent, CloneMap map) {
        super(abstractAgent, map);
        this.population = abstractAgent.population;
        this.actions = (ComponentList<GFAction>) map.clone(abstractAgent.actions, ComponentList.class);
        this.properties = (ComponentList<GFProperty>) map.clone(abstractAgent.properties, ComponentList.class);
        this.genome = map.clone(abstractAgent.genome, Genome.class);
        this.body = map.clone(abstractAgent.body, Body.class);
    }

    @Override
    public Population getPopulation() {
        return population;
    }

    @Override
    public void setPopulation(Population population) {
        this.population = population;
    }

    @Override
    public boolean addAction(GFAction action) {
        return actions.add(action);
    }

    @Override
    public boolean removeAction(GFAction action) {
        return actions.remove(action);
    }

    @Override
    public void removeAllActions() {
        actions.clear();
    }

    @Override
    public <T extends GFAction> T getAction(String name, Class<T> clazz) {
        return actions.get(name, clazz);
    }

    @Override
    public boolean addProperty(GFProperty property) {
        return properties.add(property);
    }

    @Override
    public boolean removeProperty(GFProperty property) {
        return properties.remove(property);
    }

    @Override
    public void removeAllProperties() {
        properties.clear();
    }

    @Override
    public <T extends GFProperty> T getProperty(String name, Class<T> clazz) {
        return properties.get(name, clazz);
    }

    @Override
    public boolean addGene(Gene gene) {
        return genome.add(gene);
    }

    @Override
    public boolean removeGene(Gene gene) {
        return genome.remove(gene);
    }

    @Override
    public void removeAllGenes() {
        genome.clear();
    }

    @Override
    public Iterable<Gene<?>> getGenes() {
        return genome;
    }

    @Override
    public <T extends Gene> T getGene(String name, Class<T> clazz) {
        return genome.get(name, clazz);
    }

    @Override
    public void setGenome(Genome genome) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCloneOf(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<GFComponent> getComponents() {
        return Iterables.concat(body, actions, properties, getGenes());
    }

    @Override
    public void changeActionExecutionOrder(GFAction object, GFAction object2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void pushMessage(ACLMessage message) {
        getSimulationContext().pushMessage(message);
    }

    @Override
    public void pushMessages(Iterable<? extends ACLMessage> message) {
        getSimulationContext().pushMessages(message);
    }

    @Override
    public int getId() {
        return getSimulationContext().getId();
    }

    @Override
    public int getTimeOfBirth() {
        return getSimulationContext().getTimeOfBirth();
    }

    @Override
    public int getAge() {
        return getSimulationContext().getAge();
    }

    @Override
    public Color getColor() {
        return body.getColor();
    }

    @Override
    public void setColor(Color color) {
        body.setColor(color);
    }

    @Override
    public double getRadius() {
        return body.getRadius();
    }

    @Override
    public GFAction getLastExecutedAction() {
        return getSimulationContext().getLastExecutedAction();
    }

    @Override
    public void sendMessage(ACLMessage message) {
        getSimulationContext().sendMessage(message);
    }

    @Override
    public List<ACLMessage> pullMessages(MessageTemplate template) {
        return getSimulationContext().pullMessages(template);
    }

    @Override
    public boolean hasMessages(MessageTemplate template) {
        return getSimulationContext().hasMessages(template);
    }

    @Override
    public AgentLog getLog() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<MovingObject2D> findNeighbours(double range) {
        return getSimulationContext().findNeighbours(range);
    }

    @Override
    public void execute() {

    }

    @Override
    public void shutDown() {}

    @Override
    public Simulation getSimulation() {
        return getSimulationContext().getSimulation();
    }

    @Override
    public void setSimulation(Simulation simulation) {
        throw new UnsupportedOperationException();
    }

    protected abstract SimulationContext getSimulationContext();

    @Override
    public void freeze() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void checkNotFrozen() throws IllegalStateException {}

    @Override
    public Iterator<GFComponent> iterator() {
        return getComponents().iterator();
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
    public PolarPoint getMotionVector() {
        return body.getMotionVector();
    }

    @Override
    public void setMotionVector(PolarPoint polarPoint) {
        body.setMotionVector(polarPoint);
    }

    @Override
    public void changeMotion(double angle, double velocity) {
        body.changeMotion(angle, velocity);
    }

    @Override
    public void setMotion(double angle, double velocity) {
        body.setMotion(angle, velocity);
    }

    @Override
    public Location2D getAnchorPoint() {
        return body.getAnchorPoint();
    }

    @Override
    public void setAnchorPoint(Location2D location2d) {
        body.setAnchorPoint(location2d);
    }

    @Override
    public double getOrientation() {
        return body.getOrientation();
    }

    @Override
    public void setOrientation(double alpha) {
        body.setOrientation(alpha);
    }

    @Override
    public void prepare(Simulation context) {}
}
