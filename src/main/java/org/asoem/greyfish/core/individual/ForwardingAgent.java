package org.asoem.greyfish.core.individual;

import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.genes.GeneComponent;
import org.asoem.greyfish.core.genes.GeneComponentList;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.MotionObject2D;

import javax.annotation.Nullable;
import java.awt.*;

public abstract class ForwardingAgent extends ForwardingObject implements Agent {

    @Override
    protected abstract Agent delegate();

    @Override
    public Population getPopulation() {
        return delegate().getPopulation();
    }

    @Override
    public void setPopulation(Population population) {
        delegate().setPopulation(population);
    }

    /**
     * If you wish overwrite {@link Agent#addAction}, make sure to call {@link AgentComponent#setAgent} on {@code action} after addition.
     */
    @Override
    public boolean addAction(GFAction action) {
        return delegate().addAction(action);
    }

    @Override
    public boolean removeAction(GFAction action) {
        return delegate().removeAction(action);
    }

    @Override
    public void removeAllActions() {
        delegate().removeAllActions();
    }

    @Override
    public ComponentList<GFAction> getActions() {
        return delegate().getActions();
    }

    /**
     * If you wish overwrite {@link Agent#addProperty}, make sure to call {@link AgentComponent#setAgent} on {@code action} after addition.
     */
    @Override
    public boolean addProperty(GFProperty property) {
        return delegate().addProperty(property);
    }

    @Override
    public boolean removeProperty(GFProperty property) {
        return delegate().removeProperty(property);
    }

    @Override
    public void removeAllProperties() {
        delegate().removeAllProperties();
    }

    @Override
    public ComponentList<GFProperty> getProperties() {
        return delegate().getProperties();
    }

    @Override
    @Nullable
    public <T extends GFProperty> T getProperty(String name, Class<T> propertyClass) {
        return delegate().getProperty(name, propertyClass);
    }

    @Override
    public boolean isCloneOf(Object object) {
        return delegate().isCloneOf(object);
    }

    @Override
    public Iterable<AgentComponent> getComponents() {
        return delegate().getComponents();
    }

    @Override
    public AgentComponent getComponent(String name) {
        return delegate().getComponent(name);
    }

    @Override
    public void changeActionExecutionOrder(GFAction object, GFAction object2) {
        delegate().changeActionExecutionOrder(object, object2);
    }

    @Override
    public void freeze() {
        delegate().freeze();
    }

    @Override
    public boolean isFrozen() {
        return delegate().isFrozen();
    }

    @Override
    public int getId() {
        return delegate().getId();
    }

    @Override
    public int getTimeOfBirth() {
        return delegate().getTimeOfBirth();
    }

    @Override
    public Color getColor() {
        return delegate().getColor();
    }

    @Override
    public void setColor(Color color) {
        delegate().setColor(color);
    }

    @Override
    public GFAction getLastExecutedAction() {
        return delegate().getLastExecutedAction();
    }

    @Override
    public Body getBody() {
        return delegate().getBody();
    }

    @Override
    public Iterable<AgentMessage> getMessages(MessageTemplate template) {
        return delegate().getMessages(template);
    }

    @Override
    public boolean hasMessages(MessageTemplate template) {
        return delegate().hasMessages(template);
    }

    @Override
    public void logEvent(Object eventOrigin, String title, String message) {
        delegate().logEvent(eventOrigin, title, message);
    }

    @Override
    public void receiveAll(Iterable<? extends AgentMessage> messages) {
        delegate().receiveAll(messages);
    }

    @Override
    public void receive(AgentMessage messages) {
        delegate().receive(messages);
    }

    @Override
    public int getAge() {
        return delegate().getAge();
    }

    @Override
    public void execute() {
        delegate().execute();
    }

    @Override
    public void shutDown() {
        delegate().shutDown();
    }

    @Override
    public boolean isActive() {
        return delegate().isActive();
    }

    @Override
    public Simulation simulation() {
        return delegate().simulation();
    }

    @Override
    public void activate(Simulation context) {
        delegate().activate(context);
    }

    @Override
    public <T extends GFAction> T getAction(String actionName, Class<T> gfActionClass) {
        return delegate().getAction(actionName, gfActionClass);
    }

    @Override
    public <T extends GeneComponent> T getGene(String geneName, Class<T> geneClass) {
        return delegate().getGene(geneName, geneClass);
    }

    @Override
    public boolean addGene(GeneComponent<?> gene) {
        return delegate().addGene(gene);
    }

    @Override
    public boolean removeGene(GeneComponent<?> gene) {
        return delegate().removeGene(gene);
    }

    @Override
    public void removeAllGenes() {
        delegate().removeAllGenes();
    }

    @Override
    public GeneComponentList<GeneComponent<?>> getGeneComponentList() {
        return delegate().getGeneComponentList();
    }

    @Override
    public Motion2D getMotion() {
        return delegate().getMotion();
    }

    @Override
    public void setMotion(Motion2D motion) {
        delegate().setMotion(motion);
    }

    @Override
    public MotionObject2D getProjection() {
        return delegate().getProjection();
    }

    @Override
    public void setProjection(MotionObject2D projection) {
        delegate().setProjection(projection);
    }

    @Override
    public void initialize() {
        delegate().initialize();
    }

    @Override
    public void updateGeneComponents(Chromosome vector) {
        delegate().updateGeneComponents(vector);
    }

    @Override
    public boolean didCollide() {
        return delegate().didCollide();
    }

    @Override
    public Iterable<AgentComponent> children() {
        return delegate().children();
    }
}
