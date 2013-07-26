package org.asoem.greyfish.core.actions;

import org.apache.commons.math3.util.FastMath;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.utils.base.Callback;
import org.asoem.greyfish.utils.base.Callbacks;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.base.Tagged;
import org.asoem.greyfish.utils.math.RandomGenerators;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.asoem.greyfish.utils.space.Motion2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@Tagged("actions")
public class GenericMovement<A extends SpatialAgent<A, ?, ?>> extends AbstractAgentAction<A> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericMovement.class);

    private Callback<? super GenericMovement<A>, Double> stepSize;
    private Callback<? super GenericMovement<A>, Double> turningAngle;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public GenericMovement() {
        this(new Builder<A>());
    }

    private GenericMovement(final GenericMovement<A> cloneable, final DeepCloner map) {
        super(cloneable, map);
        this.stepSize = cloneable.stepSize;
        this.turningAngle = cloneable.turningAngle;
    }

    protected GenericMovement(final AbstractBuilder<A, ? extends GenericMovement<A>, ? extends AbstractBuilder<A,?,?>> builder) {
        super(builder);
        this.stepSize = builder.stepSize;
        this.turningAngle = builder.turningAngle;
    }

    @Override
    protected ActionState proceed() {
        final double evaluatedTurningAngle = Callbacks.call(turningAngle, this);
        final double evaluatedStepSize = Callbacks.call(stepSize, this);

        final Motion2D motion = ImmutableMotion2D.of(evaluatedTurningAngle, evaluatedStepSize);
        agent().setMotion(motion);

        LOGGER.info("{}: Changing movement to {}", agent(), motion);
        return ActionState.COMPLETED;
    }

    @Override
    public GenericMovement<A> deepClone(final DeepCloner cloner) {
        return new GenericMovement<A>(this, cloner);
    }


    public static <A extends SpatialAgent<A, ?, ?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public Callback<? super GenericMovement<A>, Double> getStepSize() {
        return stepSize;
    }

    public Callback<? super GenericMovement<A>, Double> getTurningAngle() {
        return turningAngle;
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static final class Builder<A extends SpatialAgent<A, ?, ?>> extends AbstractBuilder<A, GenericMovement<A>, Builder<A>> implements Serializable {
        private Builder() {
        }

        private Builder(final GenericMovement<A> genericMovement) {
            super(genericMovement);
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected GenericMovement<A> checkedBuild() {
            return new GenericMovement<A>(this);
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

    @SuppressWarnings({"UnusedDeclaration"})
    protected static abstract class AbstractBuilder<A extends SpatialAgent<A, ?, ?>, C extends GenericMovement<A>, B extends AbstractBuilder<A, C, B>> extends AbstractAgentAction.AbstractBuilder<A, C, B> implements Serializable {
        private Callback<? super GenericMovement<A>, Double> stepSize = Callbacks.constant(0.1);
        private Callback<? super GenericMovement<A>, Double> turningAngle = new Callback<GenericMovement<A>, Double>() {
            @Override
            public Double apply(final GenericMovement<A> caller, final Map<String, ?> args) {
                return RandomGenerators.rnorm(RandomGenerators.rng(), 0.0, FastMath.PI / 2);
            }
        };

        protected AbstractBuilder(final GenericMovement<A> genericMovement) {
            super(genericMovement);
            this.stepSize = genericMovement.stepSize;
            this.turningAngle = genericMovement.turningAngle;
        }

        protected AbstractBuilder() {
        }

        public B turningAngle(final Callback<? super GenericMovement<A>, Double> rotation) {
            this.turningAngle = checkNotNull(rotation);
            return self();
        }

        public B stepSize(final Callback<? super GenericMovement<A>, Double> speedFunction) {
            this.stepSize = checkNotNull(speedFunction);
            return self();
        }
    }
}
