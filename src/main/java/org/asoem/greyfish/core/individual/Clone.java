package org.asoem.greyfish.core.individual;

import org.asoem.greyfish.core.actions.GFAction;
import org.asoem.greyfish.core.properties.GFProperty;
import org.asoem.greyfish.core.simulation.Simulation;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * User: christoph
 * Date: 09.09.11
 * Time: 18:00
 */
public class Clone extends AgentDecorator {

    /**
     * @param delegate the prototype
     * @param simulation the simulation context
     * @see AgentDecorator#AgentDecorator(Agent)
     */
    public Clone(@Nonnull Prototype delegate, @Nonnull Simulation simulation) {
        super(delegate.delegate().deepClone(Agent.class));
        delegate.setSimulation(checkNotNull(simulation));
    }

    @Override
    public boolean addAction(GFAction action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addProperty(GFProperty property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAction(GFAction action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllActions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeProperty(GFProperty property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPopulation(Population population) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(Simulation context) {
        checkState(checkNotNull(context) == getSimulation());
        super.prepare(context);
    }

    @Override
    public void setSimulation(Simulation simulation) {
        throw new UnsupportedOperationException();
    }
}
