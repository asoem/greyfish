package org.asoem.greyfish.core.actions;

import com.google.common.reflect.TypeToken;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.utils.base.*;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.asoem.greyfish.utils.space.Motion2D;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

@Tagged("actions")
public class GenericMovement extends AbstractAgentAction {

    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(GenericMovement.class);

    private Callback<? super GenericMovement, Double> stepSize;
    private Callback<? super GenericMovement, Double> turningAngle;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public GenericMovement() {
        this(new Builder());
    }

    private GenericMovement(GenericMovement cloneable, DeepCloner map) {
        super(cloneable, map);
        this.stepSize = cloneable.stepSize;
        this.turningAngle = cloneable.turningAngle;
    }

    protected GenericMovement(AbstractBuilder<? extends GenericMovement, ? extends AbstractBuilder> builder) {
        super(builder);
        this.stepSize = builder.stepSize;
        this.turningAngle = builder.turningAngle;
    }

    @Override
    protected ActionState proceed(Simulation simulation) {
        final double evaluatedTurningAngle = Callbacks.call(turningAngle, this);
        final double evaluatedStepSize = Callbacks.call(stepSize, this);

        final Motion2D motion = ImmutableMotion2D.of(evaluatedTurningAngle, evaluatedStepSize);
        agent().setMotion(motion);

        LOGGER.info("{}: Changing movement to {}", agent(), motion);
        return ActionState.COMPLETED;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Step Size", TypedValueModels.forField("stepSize", this, new TypeToken<Callback<? super GenericMovement, Double>>() {
        }));
        e.add("Turning Angle", TypedValueModels.forField("turningAngle", this, new TypeToken<Callback<? super GenericMovement, Double>>() {
        }));
    }

    @Override
    public GenericMovement deepClone(DeepCloner cloner) {
        return new GenericMovement(this, cloner);
    }


    public static Builder builder() {
        return new Builder();
    }

    public Callback<? super GenericMovement, Double> getStepSize() {
        return stepSize;
    }

    public Callback<? super GenericMovement, Double> getTurningAngle() {
        return turningAngle;
    }

    private Object writeReplace() {
        return new Builder(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static final class Builder extends AbstractBuilder<GenericMovement, Builder> implements Serializable {
        private Builder() {
        }

        private Builder(GenericMovement genericMovement) {
            super(genericMovement);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected GenericMovement checkedBuild() {
            return new GenericMovement(this);
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
    protected static abstract class AbstractBuilder<C extends GenericMovement, B extends AbstractBuilder<C, B>> extends AbstractAgentAction.AbstractBuilder<C, B> implements Serializable {
        private Callback<? super GenericMovement, Double> stepSize = Callbacks.constant(0.1);
        private Callback<? super GenericMovement, Double> turningAngle = new Callback<GenericMovement, Double>() {
            @Override
            public Double apply(GenericMovement caller, Arguments arguments) {
                return RandomUtils.rnorm(0.0, MathLib.HALF_PI);
            }
        };

        protected AbstractBuilder(GenericMovement genericMovement) {
            super(genericMovement);
            this.stepSize = genericMovement.stepSize;
            this.turningAngle = genericMovement.turningAngle;
        }

        protected AbstractBuilder() {
        }

        public B turningAngle(Callback<? super GenericMovement, Double> rotation) {
            this.turningAngle = checkNotNull(rotation);
            return self();
        }

        public B stepSize(Callback<? super GenericMovement, Double> speedFunction) {
            this.stepSize = checkNotNull(speedFunction);
            return self();
        }
    }
}
