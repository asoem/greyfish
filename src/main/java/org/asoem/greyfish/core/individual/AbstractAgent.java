package org.asoem.greyfish.core.individual;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Gene;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.collect.TreeNode;
import org.asoem.greyfish.utils.collect.Trees;
import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
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
@Root(name = "agent")
public abstract class AbstractAgent implements Agent {

    @Element(name = "properties")
    protected final ComponentList<GFProperty> properties;

    @Element(name = "actions")
    protected final ComponentList<GFAction> actions;

    @Element(name = "genome")
    protected final Genome<Gene<?>> genome;

    @Element(name = "body")
    protected final Body body;

    private final AgentComponent rootComponent;

    @Element(name = "population")
    protected Population population;

    protected SimulationContext simulationContext = SimulationContext.NULL_CONTEXT;

    private final AgentMessageBox inBox = new AgentMessageBox();

    @Element(name = "projection")
    private Object2D object2D;

    @SimpleXMLConstructor
    protected AbstractAgent(Body body,
                            ComponentList<GFProperty> properties,
                            ComponentList<GFAction> actions,
                            Genome<Gene<?>> genome) {
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
    protected AbstractAgent(AbstractAgent abstractAgent, DeepCloner cloner) {
        cloner.addClone(this);
        this.population = abstractAgent.population;
        this.actions = (ComponentList<GFAction>) cloner.cloneField(abstractAgent.actions, ComponentList.class);
        this.properties = (ComponentList<GFProperty>) cloner.cloneField(abstractAgent.properties, ComponentList.class);
        this.genome = cloner.cloneField(abstractAgent.genome, Genome.class);
        this.body = cloner.cloneField(abstractAgent.body, Body.class);

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
        return actions.find(name, clazz);
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
        return properties.find(name, clazz);
    }

    @Override
    public boolean addGene(Gene<?> gene) {
        return addComponent(genome, gene);
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
    public Genome<Gene<?>> getGenome() {
        return genome;
    }

    @Override
    @Nullable
    public <T extends Gene> T getGene(String name, Class<T> clazz) {
        checkNotNull(clazz);

        return genome.find(name, clazz);
    }

    @Override
    public void injectGamete(Genome<? extends Gene<?>> genome) {
        this.genome.updateAllGenes(genome);
    }

    @Override
    public void initGenome() {
        genome.initGenes();
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
    public int getId() {
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
        return population.getColor();
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
    public void changeMotion(double angle, double velocity) {
        body.changeMotion(angle, velocity);
    }

    @Override
    public void setMotion(double angle, double velocity) {
        body.setMotion(angle, velocity);
    }

    @Override
    public void setRotation(double alpha) {
        body.setRotation(alpha);
    }

    @Override
    public void setTranslation(double speed) {
        body.setTranslation(speed);
    }

    @Override
    public Motion2D getMotion() {
        return body.getMotion2D();
    }

    @Override
    public void prepare(Simulation context) {
        setSimulation(context);
        for (AgentComponent component : getComponents()) {
            component.prepare(context);
        }
    }

    @Override
    public ComponentList<GFProperty> getProperties() {
        return properties;
    }

    @Override
    public ComponentList<GFAction> getActions() {
        return actions;
    }

    @Override
    public Body getBody() {
        return body;
    }

    @Override
    public Object2D getProjection() {
        return object2D;
    }

    @Override
    public void setProjection(Object2D projection) {
        this.object2D = projection;
    }

    protected static abstract class AbstractBuilder<E extends AbstractAgent, T extends AbstractBuilder<E,T>> extends org.asoem.greyfish.utils.base.AbstractBuilder<E,T> {
        protected final ComponentList<GFAction> actions = new MutableComponentList<GFAction>();
        protected final ComponentList<GFProperty> properties =  new MutableComponentList<GFProperty>();
        protected final Population population;
        public final ComponentList<Gene<?>> genes = new MutableComponentList<Gene<?>>();

        protected AbstractBuilder(Population population) {
            this.population = checkNotNull(population, "Population must not be null");
        }

        // todo: these builder methods are not able to control the mutability of the genes
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
                        return agentComponentTreeNode == null ? Iterators.<AgentComponent>emptyIterator() : agentComponentTreeNode.children().iterator();
                    }
                });
            }
        };
    }
}
