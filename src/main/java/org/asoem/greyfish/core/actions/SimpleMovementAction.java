package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.space.ImmutableMotion2D;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags = "actions")
public class SimpleMovementAction extends AbstractGFAction {

    @Element(required=false)
    private GreyfishExpression speed;

    @Element(required=false)
    private GreyfishExpression rotation;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public SimpleMovementAction() {
        this(new Builder());
    }

    private SimpleMovementAction(SimpleMovementAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.speed = cloneable.speed;
        this.rotation = cloneable.rotation;
    }

    protected SimpleMovementAction(AbstractBuilder<?, ?> builder) {
        super(builder);
        this.speed = builder.speed;
        this.rotation = builder.rotation;
    }

    @Override
    protected ActionState proceed(Simulation simulation) {
        final double angle = rotation.evaluateForContext(this).asDouble();
        final double velocity = speed.evaluateForContext(this).asDouble();

        agent().setMotion(ImmutableMotion2D.of(angle, velocity));

        return ActionState.SUCCESS;
    }

    @Override
    public boolean checkPreconditions(Simulation simulation) {
        return super.checkPreconditions(simulation);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Speed", TypedValueModels.forField("speed", this, GreyfishExpression.class));
        e.add("Rotation", TypedValueModels.forField("rotation", this, GreyfishExpression.class));
    }

    @Override
    public SimpleMovementAction deepClone(DeepCloner cloner) {
        return new SimpleMovementAction(this, cloner);
    }


    public static Builder builder() { return new Builder(); }

    public GreyfishExpression getSpeed() {
        return speed;
    }

    public GreyfishExpression getRotation() {
        return rotation;
    }

    public static final class Builder extends AbstractBuilder<SimpleMovementAction, Builder> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override protected SimpleMovementAction checkedBuild() { return new SimpleMovementAction(this); }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected static abstract class AbstractBuilder<E extends SimpleMovementAction, T extends AbstractBuilder<E,T>> extends AbstractGFAction.AbstractBuilder<E,T> {
        private GreyfishExpression speed = GreyfishExpressionFactoryHolder.compile("0.1");
        private GreyfishExpression rotation = GreyfishExpressionFactoryHolder.compile("rnorm(0.0, HALF_PI)");

        public T rotation(GreyfishExpression rotation) { this.rotation = checkNotNull(rotation); return self(); }
        public T speed(GreyfishExpression speedFunction) { this.speed = checkNotNull(speedFunction); return self(); }
    }
}
