package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Attribute;

@ClassGroup(tags = "actions")
public class RandomMovementAction extends AbstractGFAction {

    @Attribute(required=false)
    private double speed;

    private MovementPattern pattern = MovementPatterns.noMovement();

    private RandomMovementAction() {
        this(new Builder());
    }

    @Override
    protected void performAction(Simulation simulation) {
        pattern.apply(Agent.class.cast(getComponentOwner()), simulation);
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.add(new ValueAdaptor<Double>("", Double.class) {

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
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        pattern = MovementPatterns.borderAvoidanceMovement(speed, 0.3);
    }

    @Override
    public RandomMovementAction deepCloneHelper(CloneMap cloneMap) {
        return new RandomMovementAction(this, cloneMap);
    }

    private RandomMovementAction(RandomMovementAction cloneable, CloneMap map) {
        super(cloneable, map);
        this.speed = cloneable.speed;
    }

    protected RandomMovementAction(AbstractBuilder<?> builder) {
        super(builder);
        this.speed = builder.speed;
    }

    public static Builder with() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<RandomMovementAction> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public RandomMovementAction build() { return new RandomMovementAction(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFAction.AbstractBuilder<T> {
        private double speed = 0.1;

        public T speed(double speed) { this.speed = speed; return self(); }
    }
}
