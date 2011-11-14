package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.ValueAdaptor;
import org.simpleframework.xml.Element;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags = "actions")
public class RandomMovementAction extends AbstractGFAction {

    @Nonnull
    @Element(required=false)
    private GreyfishExpression speedFunction;

    @Nonnull
    private MovementPattern pattern = MovementPatterns.noMovement();

    @SimpleXMLConstructor
    public RandomMovementAction() {
        this(new Builder());
    }

    @Override
    protected ActionState executeUnconditioned(Simulation simulation) {
        pattern.apply(agent(), simulation);
        double speed = speedFunction.evaluateAsDouble(this);
        agent().setTranslation(speed);
        return ActionState.END_SUCCESS;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Speed", new ValueAdaptor<GreyfishExpression>(GreyfishExpression.class) {

            @Override
            protected void set(GreyfishExpression arg0) {
                speedFunction = GreyfishExpression.compile(arg0.getExpression());
            }

            @Override
            public GreyfishExpression get() {
                return speedFunction;
            }
        });
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        pattern = MovementPatterns.borderAvoidanceMovement(speedFunction.evaluateAsDouble(this), 0.1);
    }

    @Override
    public RandomMovementAction deepClone(DeepCloner cloner) {
        return new RandomMovementAction(this, cloner);
    }

    private RandomMovementAction(RandomMovementAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.speedFunction = cloneable.speedFunction;
    }

    protected RandomMovementAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.speedFunction = builder.speedFunction;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<RandomMovementAction, Builder> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public RandomMovementAction checkedBuild() { return new RandomMovementAction(this); }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected static abstract class AbstractBuilder<E extends RandomMovementAction, T extends AbstractBuilder<E,T>> extends AbstractGFAction.AbstractBuilder<E,T> {
        private GreyfishExpression speedFunction = GreyfishExpression.compile("0");

        public T speed(GreyfishExpression speedFunction) { this.speedFunction = checkNotNull(speedFunction); return self(); }
    }
}
