package org.asoem.greyfish.core.traits;

import com.google.common.base.Optional;
import org.asoem.greyfish.core.agent.AbstractAgentComponent;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.utils.base.TypedSupplier;

import javax.annotation.Nullable;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractTrait<A extends Agent<A, ?>, T> extends AbstractAgentComponent<A> implements AgentTrait<A, T> {

    @Nullable
    private A agent;

    protected AbstractTrait() {
    }

    public AbstractTrait(final AbstractBuilder<A, ? extends AbstractTrait<A, T>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + ":" + String.valueOf(get()) + "]";
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
    public T segregate(final T allele1, final T allele2) {
        return createInitialValue();
    }

    @Override
    public T mutate(final T allele) {
        return createInitialValue();
    }

    /**
     * @return this components optional {@code Agent}
     */
    public final Optional<A> agent() {
        return Optional.fromNullable(agent);
    }

    public final void setAgent(@Nullable final A agent) {
        this.agent = agent;
    }
}
