package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.CloneMap;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

@Tagged("actions")
public class Suicide<A extends Agent<A, ?>> extends AbstractAgentAction<A> {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(Suicide.class);

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private Suicide() {
        this(new Builder<A>());
    }

    private Suicide(Suicide<A> cloneable, CloneMap map) {
        super(cloneable, map);
    }

    private Suicide(AbstractBuilder<A, ? extends Suicide<A>, ? extends AbstractBuilder<A,?,?>> builder) {
        super(builder);
    }

    @Override
    protected ActionState proceed() {
        agent().die();
        LOGGER.info("{}: Dying", agent());
        agent().logEvent(this, "dies", "");
        return ActionState.COMPLETED;
    }

    @Override
    public Suicide<A> deepClone(CloneMap cloneMap) {
        return new Suicide<A>(this, cloneMap);
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends Agent<A, ?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public static final class Builder<A extends Agent<A, ?>> extends AbstractBuilder<A, Suicide<A>, Builder<A>> implements Serializable {
        private Builder() {}

        private Builder(Suicide<A> suicide) {
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
