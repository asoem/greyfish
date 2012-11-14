package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

@Tagged("actions")
public class Suicide extends AbstractAgentAction {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(Suicide.class);

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private Suicide() {
        this(new Builder());
    }

    private Suicide(AbstractAgentAction cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    private Suicide(AbstractBuilder<? extends Suicide, ? extends AbstractBuilder> builder) {
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
    public Suicide deepClone(DeepCloner cloner) {
        return new Suicide(this, cloner);
    }

    private Object writeReplace() {
        return new Builder(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AbstractBuilder<Suicide, Builder> implements Serializable {
        private Builder() {}

        private Builder(Suicide suicide) {
            super(suicide);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected Suicide checkedBuild() {
            return new Suicide(this);
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
