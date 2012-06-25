package org.asoem.greyfish.core.actions;

import com.google.common.reflect.TypeToken;
import javolution.lang.MathLib;
import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.individual.Callback;
import org.asoem.greyfish.core.individual.Callbacks;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.asoem.greyfish.utils.math.RandomUtils;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.asoem.greyfish.utils.space.Motion2D;
import org.simpleframework.xml.Element;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags = "actions")
public class GenericMovementAction extends AbstractGFAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericMovementAction.class);

    @Element(required = false)
    private Callback<? super GenericMovementAction, Double> stepSize;

    @Element(required = false)
    private Callback<? super GenericMovementAction, Double> turningAngle;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public GenericMovementAction() {
        this(new Builder());
    }

    private GenericMovementAction(GenericMovementAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.stepSize = cloneable.stepSize;
        this.turningAngle = cloneable.turningAngle;
    }

    protected GenericMovementAction(AbstractBuilder<?, ?> builder) {
        super(builder);
        this.stepSize = builder.speed;
        this.turningAngle = builder.rotation;
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
        e.add("Step Size", TypedValueModels.forField("stepSize", this, new TypeToken<Callback<? super GenericMovementAction, Double>>() {
        }));
        e.add("Turning Angle", TypedValueModels.forField("turningAngle", this, new TypeToken<Callback<? super GenericMovementAction, Double>>() {
        }));
    }

    @Override
    public GenericMovementAction deepClone(DeepCloner cloner) {
        return new GenericMovementAction(this, cloner);
    }


    public static Builder builder() {
        return new Builder();
    }

    public Callback<? super GenericMovementAction, Double> getStepSize() {
        return stepSize;
    }

    public Callback<? super GenericMovementAction, Double> getTurningAngle() {
        return turningAngle;
    }

    public static final class Builder extends AbstractBuilder<GenericMovementAction, Builder> {
        private Builder() {
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected GenericMovementAction checkedBuild() {
            return new GenericMovementAction(this);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected static abstract class AbstractBuilder<E extends GenericMovementAction, T extends AbstractBuilder<E, T>> extends AbstractActionBuilder<E, T> {
        private Callback<? super GenericMovementAction, Double> speed = Callbacks.constant(0.1);
        private Callback<? super GenericMovementAction, Double> rotation = new Callback<GenericMovementAction, Double>() {
            @Override
            public Double apply(GenericMovementAction caller, Map<String, ?> arguments) {
                return RandomUtils.rnorm(0.0, MathLib.HALF_PI);
            }
        };

        public T turningAngle(Callback<? super GenericMovementAction, Double> rotation) {
            this.rotation = checkNotNull(rotation);
            return self();
        }

        public T stepSize(Callback<? super GenericMovementAction, Double> speedFunction) {
            this.speed = checkNotNull(speedFunction);
            return self();
        }
    }
}
