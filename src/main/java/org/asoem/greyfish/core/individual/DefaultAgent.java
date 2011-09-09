package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.istack.internal.NotNull;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.genes.ImmutableGenome;
import org.asoem.greyfish.core.genes.MutableGenome;
import org.asoem.greyfish.core.io.AgentLog;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.space.Location2D;
import org.asoem.greyfish.core.space.MovingObject2D;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.*;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.unmodifiableIterable;
import static java.util.Arrays.asList;

@Root
public class DefaultAgent extends AbstractDeepCloneable implements Agent, MovingObject2D, Preparable<Simulation> {

    @Element(name="population")
    protected Population population = Population.newPopulation("Default", Color.black);

    @ElementList(name="properties", entry="property", required=false)
    private final List<GFProperty> properties = Lists.newArrayList();

    @ElementList(name="actions", entry="action", required=false)
    private final List<GFAction> actions = Lists.newArrayList();

    private final MutableGenome genome = new MutableGenome();

    @Element(name = "body", required = false)
    protected final Body body;

    @NotNull
    protected SimulationContext simulationContext = new SimulationContext(null, this);

    @SimpleXMLConstructor
    protected DefaultAgent(
            @ElementList(name = "properties", entry = "property", required = false) final List<GFProperty> properties,
            @ElementList(name = "actions", entry = "action", required = false) final List<GFAction> actions,
            @Element(name = "body", required = false) Body body) {
        if (properties != null) this.properties.addAll(properties);
        if (actions != null) this.actions.addAll(actions);
        this.body = (body != null) ? body : Body.newInstance(this);
        for (GFComponent component : getComponents())
            component.setAgent(this);
    }

    protected DefaultAgent(DefaultAgent defaultAgent, CloneMap map) {
        super(defaultAgent, map);
        this.population = defaultAgent.population;
        this.body = map.clone(defaultAgent.body, Body.class);
        Iterables.addAll(actions, map.cloneAll(defaultAgent.actions, GFAction.class));
        Iterables.addAll(properties, map.cloneAll(defaultAgent.properties, GFProperty.class));
    }

    protected DefaultAgent(AbstractBuilder<?> builder) {
        this.population = builder.population;
        this.body = Body.newInstance(this);

        for (GFAction action : builder.actions) {
            addAction(action);
        }
        for (GFProperty property : builder.properties) {
            addProperty(property);
        }
    }

    @Override
    public void pushMessages(Iterable<? extends ACLMessage> messages) {
        simulationContext.pushMessages(messages);
    }

    @Override
    public void pushMessage(ACLMessage message) {
        simulationContext.pushMessage(message);
    }

    @Override
    public List<ACLMessage> pullMessages(MessageTemplate template) {
        return simulationContext.pullMessages(template);
    }

    @Override
    public boolean hasMessages(MessageTemplate template) {
        return simulationContext.hasMessages(template);
    }

    @Override
    public AgentLog getLog() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutDown() {
        throw new UnsupportedOperationException();
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
    public double getOrientation() {
        return body.getOrientation();
    }

    @Override
    public void setOrientation(double alpha) {
        body.setOrientation(alpha);
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
    public Iterator<GFComponent> iterator() {
        return getComponents().iterator();
    }

    @Override
    public Population getPopulation() {
        return population;
    }

    @Override
    public void setPopulation(Population population) {
        checkNotNull(population);
        this.population = population;
    }

    @Override
    public Body getBody() {
        return body;
    }

    /**
     * Adds the given actions to this individual.
     * The actions's execution level is set to the highest execution level found in this individual's actions +1;
     * @param action The action to sum
     * @return {@code true} if actions could be added, {@code false} otherwise.
     */
    @Override
    public boolean addAction(final GFAction action) {
        action.setAgent(this);
        return actions.add(action);
    }

    @Override
    public boolean removeAction(final GFAction action) {
        action.setAgent(null);
        return actions.remove(action);
    }

    @Override
    public void removeAllActions() {
        for (GFAction action : actions) {
            removeAction(action);
        }
    }

    @Override
    public Iterable<GFAction> getActions() {
        return unmodifiableIterable(actions);
    }

    /**
     * Add <code>property</code> to the Individuals properties if it does not contain one with the same key (i.e. property.getPropertyName() ).
     * @param property The property to sum
     * @return <code>true</code> if <code>property</code> could be added, <code>false</code> otherwise.
     */
    @Override
    public boolean addProperty(final GFProperty property) {
        property.setAgent(this);
        return properties.add(property);
    }

    @Override
    public boolean removeProperty(final GFProperty property) {
        property.setAgent(null);
        return properties.remove(property);
    }

    @Override
    public void removeAllProperties() {
        for (GFProperty property : properties) {
            removeProperty(property);
        }
    }

    @Override
    public Iterable<GFProperty> getProperties() {
        return unmodifiableIterable(properties);
    }

    @Override
    @Nullable
    public <T extends GFProperty> T getProperty(final String name, Class<T> propertyClass) {
        return Iterables.find(
                Iterables.filter(getProperties(), propertyClass),
                new Predicate<T>() {

                    @Override
                    public boolean apply(T object) {
                        return object.getName().equals(name);
                    }
                }, null);
    }

    @Override
    public String toString() {
        return "DefaultAgent[" + population + "]";
    }

    @SuppressWarnings("unused")
    @Commit
    private void commit() {
        for (GFComponent component : getComponents()) {
            component.setAgent(this);
        }
    }

    @Override
    public boolean isCloneOf(Object object) {
        return Agent.class.isInstance(object)
                && population.equals(Agent.class.cast(object).getPopulation());
    }

    @Override
    public Iterable<GFComponent> getComponents() {
        return Iterables.concat( ImmutableList.of(body), properties, actions);
    }

    @Override
    public void changeActionExecutionOrder(final GFAction object, final GFAction object2) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(object2);
        if ( ! actions.contains(object) || ! actions.contains(object2))
            throw new IllegalArgumentException();
        int index1 = actions.indexOf(object);
        int index2 = actions.indexOf(object2);
        actions.add(index2, actions.remove(index1));
    }

    @Override
    public void freeze() {
        for (GFComponent component : getComponents()) {
            component.checkConsistency();
            component.freeze();
        }
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void checkNotFrozen() {
        if (isFrozen()) throw new IllegalStateException("DefaultAgent is frozen");
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

    public int getTimeOfBirth() {
        return simulationContext.getTimeOfBirth();
    }

    @Override
    public void prepare(Simulation simulation) {
        Preconditions.checkNotNull(simulation);

        this.simulationContext = new SimulationContext(simulation, this);

        for (GFComponent component : this) {
            component.prepare(simulation);
        }
    }

    @Override
    public void execute() {
        simulationContext.execute();
    }

    @Override
    public void sendMessage(ACLMessage message) {
        simulationContext.sendMessage(message);
    }

    @Override
    public Genome getGenome() {
        return ImmutableGenome.copyOf(genome);
    }

    @Override
    public double getRadius() {
        return getBody().getRadius();
    }

    @Override
    public GFAction getLastExecutedAction() {
        return simulationContext.getLastExecutedAction();
    }

    @Override
    public int getId() {
        return simulationContext.getId();
    }

    public Simulation getSimulation() {
        return simulationContext.getSimulation();
    }

    @Override
    public void setSimulation(Simulation simulation) {
        simulationContext = new SimulationContext(simulation, this); // TODO: Is 'this' sufficient? DELEGATION!
    }

    @Override
    public int getAge() {
        return simulationContext.getAge();
    }

    @Override
    public Iterable<MovingObject2D> findNeighbours(double range) {
        return simulationContext.findNeighbours(range);
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new DefaultAgent(this, map);
    }

    public void setGenome(final Genome genome) {
        Preconditions.checkNotNull(genome);
        this.genome.reset(genome);
    }

    public static Builder with() {
        return new Builder();
    }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<DefaultAgent> {
        @Override
        public DefaultAgent build() {
            return new DefaultAgent(checkedSelf());
        }
        @Override
        protected Builder self() {
            return this;
        }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends org.asoem.greyfish.lang.AbstractBuilder<T> {
        private final List<GFAction> actions = Lists.newArrayList();
        private final List<GFProperty> properties =  Lists.newArrayList();
        private Population population;

        public T population(Population population) { this.population = checkNotNull(population); return self(); }
        public T addActions(GFAction ... actions) { this.actions.addAll(asList(checkNotNull(actions))); return self(); }
        public T addProperties(GFProperty ... properties) { this.properties.addAll(asList(checkNotNull(properties))); return self(); }
    }
}
