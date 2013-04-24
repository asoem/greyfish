package org.asoem.greyfish.core.genes;

import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.utils.base.DeepCloner;

import java.util.Collections;

/**
 * User: christoph
 * Date: 07.02.12
 * Time: 12:15
 */
public abstract class AbstractTrait<A extends Agent<A, ?>, T> extends AbstractAgentComponent<A> implements AgentTrait<A, T> {

    protected AbstractTrait() {}

    protected AbstractTrait(AbstractAgentComponent<A> cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    public AbstractTrait(AbstractBuilder<A, ? extends AbstractTrait<A, T>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + ":" + String.valueOf(get()) + "]";
    }

    @Override
    public double getRecombinationProbability() {
        return 0.5;
    }

    @Override
    public void set(T value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void trySet(Object o) throws ClassCastException {
        set((T) o);
    }

    @Override
    public boolean isHeritable() {
        return false;
    }

    @Override
    public Iterable<AgentNode> children() {
        return Collections.emptyList();
    }

    @Override
    public AgentNode parent() {
        return getAgent();
    }

    @Override
    public void initialize() {
        super.initialize();
        set(createInitialValue());
    }

    @Override
    public T segregate(T allele1, T allele2) {
        return createInitialValue();
    }

    @Override
    public T mutate(T allele) {
        return createInitialValue();
    }
}
