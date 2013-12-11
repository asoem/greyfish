package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * @param <A>
 * @deprecated Use a {@link GenericAction} to remove an agent from a getSimulation
 */
@Deprecated
public class Suicide<A extends Agent<A, ? extends BasicSimulationContext<?, A>>> extends BaseAgentAction<A> {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private Suicide() {
        this(new Builder<A>());
    }

    private Suicide(final AbstractBuilder<A, ? extends Suicide<A>, ? extends AbstractBuilder<A, ?, ?>> builder) {
        super(builder);
    }

    @Override
    protected ActionState proceed() {
        throw new UnsupportedOperationException("");
        /*
        agent().die();
        agent().logEvent(this, "dies", "");
        return ActionState.COMPLETED;
        */
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends Agent<A, ? extends BasicSimulationContext<?, A>>> Builder<A> builder() {
        return new Builder<A>();
    }

    public static final class Builder<A extends Agent<A, ? extends BasicSimulationContext<?, A>>> extends AbstractBuilder<A, Suicide<A>, Builder<A>> implements Serializable {
        private Builder() {
        }

        private Builder(final Suicide<A> suicide) {
            super(suicide);
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected Suicide<A> checkedBuild() {
            return new Suicide<A>(this);
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return build();
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed with: " + e.getMessage());
            }
        }

        private static final long serialVersionUID = 0;
    }
}
