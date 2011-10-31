package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.ValueAdaptor;
import org.simpleframework.xml.Attribute;

@ClassGroup(tags = "actions")
public class RandomMovementAction extends AbstractGFAction {

    @Attribute(required=false)
    private double speed;

    private MovementPattern pattern = MovementPatterns.noMovement();

    @SimpleXMLConstructor
    public RandomMovementAction() {
        this(new Builder());
    }

    @Override
    protected ActionState executeUnconditioned(Simulation simulation) {
        pattern.apply(agent(), simulation);
        return ActionState.END_SUCCESS;
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add(new ValueAdaptor<Double>("Speed", Double.class) {

            @Override
            protected void set(Double arg0) {
                speed = arg0;
            }

            @Override
            public Double get() {
                return speed;
            }
        });
    }

    @Override
    public void prepare(Simulation simulation) {
        super.prepare(simulation);
        pattern = MovementPatterns.borderAvoidanceMovement(speed, 0.3);
    }

    @Override
    public RandomMovementAction deepClone(DeepCloner cloner) {
        return new RandomMovementAction(this, cloner);
    }

    private RandomMovementAction(RandomMovementAction cloneable, DeepCloner map) {
        super(cloneable, map);
        this.speed = cloneable.speed;
    }

    protected RandomMovementAction(AbstractBuilder<?,?> builder) {
        super(builder);
        this.speed = builder.speed;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<RandomMovementAction, Builder> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public RandomMovementAction checkedBuild() { return new RandomMovementAction(this); }
    }

    protected static abstract class AbstractBuilder<E extends RandomMovementAction, T extends AbstractBuilder<E,T>> extends AbstractGFAction.AbstractBuilder<E,T> {
        private double speed = 0.1;

        public T speed(double speed) { this.speed = speed; return self(); }
    }
}
