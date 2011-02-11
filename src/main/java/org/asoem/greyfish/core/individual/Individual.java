package org.asoem.greyfish.core.individual;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.genes.Genome;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.space.Location2DInterface;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.CircularFifoBuffer;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.CloneMap;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Commit;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.unmodifiableIterable;
import static java.util.Arrays.asList;

@Root
public class Individual extends AbstractDeepCloneable implements IndividualInterface {

//    private final ListenerSupport<IndividualCompositionListener> listenerSupport = new ListenerSupport<IndividualCompositionListener>();

    @Element(name="population")
    private Population population = Population.newPopulation("Default", Color.black);

    @ElementList(inline=true, entry="property", required=false)
    private final List<GFProperty> properties = Lists.newArrayList();

    @ElementList(inline=true, entry="action", required=false)
    private final List<GFAction> actions = Lists.newArrayList();

    @Element(name = "body", required = false)
    private Body body = Body.newInstance(this);

    private final CircularFifoBuffer<ACLMessage> inBox = CircularFifoBuffer.newInstance(64);

    @Override
    public double getRadius() {
        return body.getRadius();
    }

    @Override
    public Genome getGenome() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGenome(Genome genome) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GFAction getLastExecutedAction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute() {
        throw new UnsupportedOperationException("Only Agents should get executed");
    }

    @Override
    public void addMessages(Iterable<? extends ACLMessage> messages) {
        Iterables.addAll(inBox, messages);
    }

    @Override
    public List pollMessages(MessageTemplate template) {
        List<ACLMessage> ret = Lists.newArrayList();
        Iterator<ACLMessage> iterator = inBox.listIterator();
        while (iterator.hasNext()) {
            ACLMessage message = iterator.next();
            if (template.apply(message))
                ret.add(message);
            iterator.remove();
        }
        return ret;
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
        return Iterables.<GFComponent>concat(properties, actions).iterator();
    }

    private Individual(@ElementList(inline=true, entry="property", required=false) final List<GFProperty> properties,
              @ElementList(inline=true, entry="action", required=false) final List<GFAction> actions) {
        this.properties.addAll(properties);
        this.actions.addAll(actions);
    }

    public Individual(Builder builder) {
        this.population = builder.population;

        for (GFProperty property : builder.properties.build())
            addProperty(property);
        for (GFAction property : builder.actions.build())
            addAction(property);
    }

    public Individual(final Population population) {
        this.population = population;
    }

    protected Individual(Individual individual, CloneMap map) {
        this.population = individual.population;
        this.body = map.clone(individual.body, Body.class);
        Iterables.addAll(actions, map.cloneAll(individual.actions, GFAction.class));
        Iterables.addAll(properties, map.cloneAll(individual.properties, GFProperty.class));
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
     * @param action The action to add
     * @return {@code true} if actions could be added, {@code false} otherwise.
     */
    @Override
    public boolean addAction(final GFAction action) {
        return actions.add(action);
    }

    @Override
    public boolean removeAction(final GFAction action) {
        return actions.remove(action);
    }

    @Override
    public void removeAllActions() {
        actions.clear();
    }

    @Override
    public Iterable<GFAction> getActions() {
        return unmodifiableIterable(actions);
    }

    /**
     * Add <code>property</code> to the Individuals properties if it does not contain one with the same key (i.e. property.getPropertyName() ).
     * @param property The property to add
     * @return <code>true</code> if <code>property</code> could be added, <code>false</code> otherwise.
     */
    @Override
    public boolean addProperty(final GFProperty property) {
        return properties.add(property);
    }

    @Override
    public boolean removeProperty(final GFProperty property) {
        return properties.remove(property);
    }

    @Override
    public void removeAllProperties() {
        properties.clear();
    }

    @Override
    public Iterable<GFProperty> getProperties() {
        return unmodifiableIterable(properties);
    }

    @Override
    public String toString() {
        return "Individual[" + population + "]";
    }

    @SuppressWarnings("unused")
    @Commit
    private void commit() {
        for (GFComponent component : getComponents()) {
            component.setComponentRoot(this);
        }
    }

    @Override
    public boolean isCloneOf(Object object) {
        return IndividualInterface.class.isInstance(object)
                && population.equals(IndividualInterface.class.cast(object).getPopulation());
    }

    @Override
    public Iterable<? extends GFComponent> getComponents() {
        return Iterables.concat( properties, actions);
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
    public int getId() {
        return -1;
    }

    @Override
    public int getTimeOfBirth() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Individual deepCloneHelper(CloneMap map) {
        return new Individual(this, map);
    }

    @Override
    public void freeze() {
        checkConsistency(getComponents());
        for (GFComponent component : getComponents())
            component.freeze();
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void checkConsistency(Iterable<? extends GFComponent> components) throws IllegalStateException {
        for (GFComponent component : getComponents())
            component.checkConsistency(components);
    }

    @Override
    public <T> T checkFrozen(T value) {
        checkNotFrozen();
        return value;
    }

    @Override
    public void checkNotFrozen() {
        if (isFrozen()) throw new IllegalStateException("Individual is frozen");
    }

    public static Builder with() { return new Builder(); }

    @Override
    public Location2DInterface getAnchorPoint() {
        return body.getAnchorPoint();
    }

    @Override
    public void setAnchorPoint(Location2DInterface location2d) {
        body.setAnchorPoint(location2d);
    }

    public static class Builder implements BuilderInterface<Individual> {
        private final ImmutableList.Builder<GFAction> actions = ImmutableList.builder();
        private final ImmutableList.Builder<GFProperty> properties =  ImmutableList.builder();
        private Population population;

        public Builder population(Population population) { this.population = checkNotNull(population); return this; }
        public Builder addActions(GFAction ... actions) { this.actions.addAll(asList(checkNotNull(actions))); return this; }
        public Builder addProperties(GFProperty ... properties) { this.properties.addAll(asList(checkNotNull(properties))); return this; }

        @Override
        public Individual build() {
            checkState(population != null);
            return new Individual(this);
        }
    }
}
