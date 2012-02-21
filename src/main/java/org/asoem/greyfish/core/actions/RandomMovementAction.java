package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.actions.utils.MovementPattern;
import org.asoem.greyfish.core.actions.utils.MovementPatterns;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

@ClassGroup(tags = "actions")
public class RandomMovementAction extends AbstractGFAction {

    @Element(required=false)
    private GreyfishExpression speedFunction;

    @Element
    private MovementPattern pattern = MovementPatterns.noMovement();

    @SimpleXMLConstructor
    public RandomMovementAction() {
        this(new Builder());
    }

    @Override
    protected ActionState executeUnconditioned(Simulation simulation) {
        pattern.apply(agent(), simulation);
        double speed = speedFunction.evaluateForContext(this).asDouble();
        agent().setTranslation(speed);
        return ActionState.END_SUCCESS;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("Speed", new AbstractTypedValueModel<GreyfishExpression>() {

            @Override
            protected void set(GreyfishExpression arg0) {
                speedFunction = GreyfishExpressionFactory.compile(arg0.getExpression());
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
        pattern = MovementPatterns.borderAvoidanceMovement(speedFunction.evaluateForContext(this).asDouble(), 0.1);
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
        private GreyfishExpression speedFunction = GreyfishExpressionFactory.compile("0");

        public T speed(GreyfishExpression speedFunction) { this.speedFunction = checkNotNull(speedFunction); return self(); }
    }
}
