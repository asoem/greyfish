package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.genes.AgentTrait;
import org.asoem.greyfish.core.genes.Chromosome;
import org.asoem.greyfish.core.properties.AgentProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.collect.SearchableList;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.MotionObject2D;
import org.asoem.greyfish.utils.space.SpatialObject;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Set;

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

    @Override
    public boolean hasPopulation(Population population) {
        return delegate().hasPopulation(population);
    }

    /**
     * If you wish overwrite {@link Agent#addAction}, make sure to call {@link AgentComponent#setAgent} on {@code action} after addition.
     */
    @Override
    public boolean addAction(AgentAction action) {
        return delegate().addAction(action);
    }

    @Override
    public boolean removeAction(AgentAction action) {
        return delegate().removeAction(action);
    }

    @Override
    public void removeAllActions() {
        delegate().removeAllActions();
    }

    @Override
    public SearchableList<AgentAction> getActions() {
        return delegate().getActions();
    }

    /**
     * If you wish overwrite {@link Agent#addProperty}, make sure to call {@link AgentComponent#setAgent} on {@code action} after addition.
     */
    @Override
    public boolean addProperty(AgentProperty property) {
        return delegate().addProperty(property);
    }

    @Override
    public boolean removeProperty(AgentProperty property) {
        return delegate().removeProperty(property);
    }

    @Override
    public void removeAllProperties() {
        delegate().removeAllProperties();
    }

    @Override
    public SearchableList<AgentProperty<?>> getProperties() {
        return delegate().getProperties();
    }

    @Override
    @Nullable
    public <T extends AgentProperty> T getProperty(String name, Class<T> propertyClass) {
        return delegate().getProperty(name, propertyClass);
    }

    @Override
    public AgentProperty<?> findProperty(Predicate<? super AgentProperty<?>> predicate) {
        return delegate().findProperty(predicate);
    }

    @Override
    public void changeActionExecutionOrder(AgentAction object, AgentAction object2) {
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
    public Iterable<AgentMessage<Agent>> getMessages(MessageTemplate template) {
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
    public void receiveAll(Iterable<? extends AgentMessage<Agent>> messages) {
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
    public void shutDown(PassiveSimulationContext context) {
        delegate().shutDown(PassiveSimulationContext.instance());
    }

    @Override
    public boolean isActive() {
        return delegate().isActive();
    }

    @Override
    public Simulation<SpatialObject> simulation() {
        return delegate().simulation();
    }

    @Override
    public void activate(ActiveSimulationContext context) {
        delegate().activate(context);
    }

    @Override
    public <T extends AgentAction> T getAction(String actionName, Class<T> gfActionClass) {
        return delegate().getAction(actionName, gfActionClass);
    }

    @Override
    public <T extends AgentTrait> T getTrait(String geneName, Class<T> geneClass) {
        return delegate().getTrait(geneName, geneClass);
    }

    @Override
    public boolean addTrait(AgentTrait<?> gene) {
        return delegate().addTrait(gene);
    }

    @Override
    public boolean removeGene(AgentTrait<?> gene) {
        return delegate().removeGene(gene);
    }

    @Override
    public void removeAllGenes() {
        delegate().removeAllGenes();
    }

    @Override
    public SearchableList<AgentTrait<?>> getTraits() {
        return delegate().getTraits();
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
    public AgentTrait<?> findTrait(Predicate<? super AgentTrait<?>> traitPredicate) {
        return delegate().findTrait(traitPredicate);
    }

    @Override
    public Iterable<AgentNode> children() {
        return delegate().children();
    }

    @Override
    public AgentNode parent() {
        return delegate().parent();
    }

    @Override
    public Set<Integer> getParents() {
        return delegate().getParents();
    }
}
