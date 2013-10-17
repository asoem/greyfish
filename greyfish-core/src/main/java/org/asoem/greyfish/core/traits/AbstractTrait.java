package org.asoem.greyfish.core.traits;

import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.TypedSupplier;

import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractTrait<A extends Agent<A, ?>, T> extends AbstractAgentComponent<A> implements AgentTrait<A, T> {

    protected AbstractTrait() {}

    protected AbstractTrait(final AbstractAgentComponent<A> cloneable, final DeepCloner map) {
        super(cloneable, map);
    }

    public AbstractTrait(final AbstractBuilder<A, ? extends AbstractTrait<A, T>, ? extends AbstractBuilder<A, ?, ?>> builder) {
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
    public void set(final T value) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked") // should be safe if TypedSupplier is implemented correctly
    @Override
    public void copyFrom(final TypedSupplier<?> supplier) {
        checkNotNull(supplier);
        checkArgument(supplier.getValueType().equals(this.getValueType()));
        set((T) supplier.get());
    }

    @Override
    public Iterable<AgentNode> children() {
        return Collections.emptyList();
    }

    @Override
    public AgentNode parent() {
        return agent().orNull();
    }

    @Override
    public T segregate(final T allele1, final T allele2) {
        return createInitialValue();
    }

    @Override
    public T mutate(final T allele) {
        return createInitialValue();
    }
}
