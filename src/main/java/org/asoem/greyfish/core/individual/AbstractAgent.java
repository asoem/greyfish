package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.ForwardingGene;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.TreeNode;
import org.asoem.greyfish.utils.collect.Trees;
import org.asoem.greyfish.utils.space.MotionVector2D;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.core.Commit;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * User: christoph
 * Date: 19.09.11
 * Time: 16:20
 */
public abstract class AbstractAgent implements Agent {

    @ElementList(name="properties", entry="property", required=false)
    protected final ComponentList<GFProperty> properties;

    @ElementList(name="actions", entry="action", required=false)
    protected final ComponentList<GFAction> actions;

    @Element(name="genome", required=false)
    protected final Genome<ForwardingGene<?>> genome;

    @Element(name = "body", required = false)
    protected final Body body;

    private final AgentComponent rootComponent;

    protected Population population;

    protected SimulationContext simulationContext = SimulationContext.NULL_CONTEXT;

    private final AgentMessageBox inBox = new AgentMessageBox();

    @SimpleXMLConstructor
    protected AbstractAgent(@Element(name = "body", required = false) Body body,
                            @ElementList(name="properties", entry="property", required=false) ComponentList<GFProperty> properties,
                            @ElementList(name="actions", entry="action", required=false) ComponentList<GFAction> actions,
                            @Element(name="genome", required=false) Genome<ForwardingGene<?>> genome) {
        this.body = checkNotNull(body);
        this.properties = checkNotNull(properties);
        this.actions = checkNotNull(actions);
        this.genome = checkNotNull(genome);

        rootComponent = new AgentComponentWrapper(Iterables.concat(
                Collections.singleton(body),
                properties,
                actions,
                genome
        ));
        rootComponent.setAgent(this);
    }

    @Commit
    private void commit() {
        for (AgentComponent component : getComponents())
            component.setAgent(this);
    }

    @SuppressWarnings("unchecked")
    protected AbstractAgent(AbstractAgent abstractAgent, DeepCloner map) {
        map.setAsCloned(abstractAgent, this);
        this.population = abstractAgent.population;
        this.actions = (ComponentList<GFAction>) map.cloneField(abstractAgent.actions, ComponentList.class);
        this.properties = (ComponentList<GFProperty>) map.cloneField(abstractAgent.properties, ComponentList.class);
        this.genome = map.cloneField(abstractAgent.genome, Genome.class);
        this.body = map.cloneField(abstractAgent.body, Body.class);

        rootComponent = new AgentComponentWrapper(Iterables.concat(
                Collections.singleton(body),
                properties,
                actions,
                genome
        ));
        rootComponent.setAgent(this);
    }

    @Override
    public Population getPopulation() {
        return population;
    }

    @Override
    public void setPopulation(Population population) {
        this.population = population;
    }

    private <E extends AgentComponent> boolean addComponent(ComponentList<E> list, E element) {
        if (list.add(element)) {
            element.setAgent(this);
            return true;
        }
        return false;
    }

    private static <E extends AgentComponent> boolean removeComponent(ComponentList<? extends E> list, E element) {
        if (list.remove(element)) {
            element.setAgent(null);
            return true;
        }
        return false;
    }

    private static void clearComponentList(ComponentList<? extends AgentComponent> list) {
        List<AgentComponent> temp = ImmutableList.copyOf(list);
        list.clear();
        for (AgentComponent component : temp)
            component.setAgent(null);
    }

    @Override
    public boolean addAction(GFAction action) {
        return addComponent(actions, action);
    }

    @Override
    public boolean removeAction(GFAction action) {
        return removeComponent(actions, action);
    }

    @Override
    public void removeAllActions() {
        clearComponentList(actions);
    }

    @Override
    public <T extends GFAction> T getAction(String name, Class<T> clazz) {
        return actions.get(name, clazz);
    }

    @Override
    public boolean addProperty(GFProperty property) {
        return addComponent(properties, property);
    }

    @Override
    public boolean removeProperty(GFProperty property) {
        return removeComponent(properties, property);
    }

    @Override
    public void removeAllProperties() {
        clearComponentList(properties);
    }

    @Override
    public <T extends GFProperty> T getProperty(String name, Class<T> clazz) {
        return properties.get(name, clazz);
    }

    @Override
    public boolean addGene(Gene<?> gene) {
        return addComponent(genome, ForwardingGene.newInstance(gene));
    }

    @Override
    public boolean removeGene(Gene<?> gene) {
        return removeComponent(genome, gene);
    }

    @Override
    public void removeAllGenes() {
        clearComponentList(genome);
    }

    @Override
    public Iterable<Gene<?>> getGenes() {
        return Collections.<Gene<?>>unmodifiableCollection(genome);
    }

    @Override
    @Nullable
    public <T extends Gene> T getGene(String name, Class<T> clazz) {
        checkNotNull(clazz);

        ForwardingGene forwardingGene = genome.get(name, ForwardingGene.class);
        if (forwardingGene != null) {
            return clazz.cast(forwardingGene.getDelegate());
        }
        else
            return null;
    }

    @Override
    public void injectGamete(Genome genome) {
        throw new UnsupportedOperationException();
    }

    /**
     * WARNING: This implementation just checks if {@code object} is an {@link Agent} and shares the same population with this agent.
     * It does not check if they are or have been derived from the same prototype via {@link #deepClone}.
     */
    @Override
    public boolean isCloneOf(Object object) {
        // TODO: should be implemented differently. See JavaDoc.
        return object != null
                && Agent.class.isInstance(object) && population.equals(Agent.class.cast(object).getPopulation());

    }

    @Override
    public TreeNode<AgentComponent> getRootComponent() {
        return rootComponent;
    }

    @Override
    public void changeActionExecutionOrder(GFAction object, GFAction object2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void receive(AgentMessage message) {
        inBox.push(message);
    }

    @Override
    public void receiveAll(Iterable<? extends AgentMessage> messages) {
        inBox.pushAll(messages);
    }

    @Override
    public Integer getId() {
        return simulationContext.getId();
    }

    @Override
    public int getTimeOfBirth() {
        return simulationContext.getTimeOfBirth();
    }

    @Override
    public int getAge() {
        return simulationContext.getAge();
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
        return simulationContext.getLastExecutedAction();
    }

    @Override
    public List<AgentMessage> pullMessages(MessageTemplate template) {
        return inBox.pull(template);
    }

    @Override
    public boolean hasMessages(MessageTemplate template) {
        return Iterables.any(inBox, template);
    }

    @Override
    public void execute() {
        simulationContext.execute();
    }

    @Override
    public void shutDown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Simulation getSimulation() {
        return simulationContext.getSimulation();
    }

    @Override
    public void setSimulation(Simulation simulation) {
        this.simulationContext = new SimulationContext(simulation, this);
    }

    @Override
    public void freeze() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void checkNotFrozen() {
        if (isFrozen())
            throw new IllegalStateException("MutableAgent is frozen");
    }

    @Override
    public Iterator<AgentComponent> iterator() {
        return getComponents().iterator();
    }

    @Override
    public MotionVector2D getMotionVector() {
        return body.getMotionVector();
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
    public void setOrientation(double alpha) {
        body.setOrientation(alpha);
    }

    @Override
    public void prepare(Simulation context) {
        for (AgentComponent component : getComponents()) {
            component.prepare(context);
        }
    }

    @Override
    public Genome<Gene<?>> createGamete() {
        return genome;
    }

    @Override
    public Iterable<GFProperty> getProperties() {
        return properties;
    }

    @Override
    public Iterable<GFAction> getActions() {
        return actions;
    }

    @Override
    public Body getBody() {
        return body;
    }

    protected static abstract class AbstractBuilder<E extends AbstractAgent, T extends AbstractBuilder<E,T>> extends org.asoem.greyfish.utils.base.AbstractBuilder<E,T> {
        protected final ComponentList<GFAction> actions = new MutableComponentList<GFAction>();
        protected final ComponentList<GFProperty> properties =  new MutableComponentList<GFProperty>();
        protected Population population;
        public final ComponentList<Gene<?>> genes = new MutableComponentList<Gene<?>>();

        protected AbstractBuilder(Population population) {
            this.population = checkNotNull(population, "Population must not be null");
        }

        public T addGenes(Gene<?> ... genes) { this.genes.addAll(asList(checkNotNull(genes))); return self(); }
        public T addActions(GFAction ... actions) { this.actions.addAll(asList(checkNotNull(actions))); return self(); }
        public T addProperties(GFProperty ... properties) { this.properties.addAll(asList(checkNotNull(properties))); return self(); }
    }

    @Override
    public Iterable<AgentComponent> getComponents() {
        return new Iterable<AgentComponent>() {
            @Override
            public Iterator<AgentComponent> iterator() {
                return Trees.postOrderView(rootComponent, new Function<TreeNode<AgentComponent>, Iterator<AgentComponent>>() {
                    @Override
                    public Iterator<AgentComponent> apply(@Nullable TreeNode<AgentComponent> agentComponentTreeNode) {
                        return checkNotNull(agentComponentTreeNode).children().iterator();
                    }
                });
            }
        };
    }
}
