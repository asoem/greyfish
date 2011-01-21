package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.interfaces.Movement2DAcutator;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.AbstractDeepCloneable;
import org.asoem.greyfish.utils.Exporter;
import org.asoem.greyfish.utils.RandomUtils;
import org.asoem.greyfish.utils.ValueAdaptor;
import org.simpleframework.xml.Attribute;

import java.util.Map;

@ClassGroup(tags = "actions")
public class RandomMovementAction extends AbstractGFAction {

    @Attribute(required=false)
    private double speed;

    private Movement2DAcutator movementAcutator;

    private RandomMovementAction() {
        this(new Builder());
    }

    @Override
    protected void performAction(Simulation simulation) {
        // rotate
        if (RandomUtils.nextBoolean()) {
            float phi = RandomUtils.nextFloat(0f, 0.1f);
            movementAcutator.rotate(simulation, phi);
        }

        // translate
        movementAcutator.translate(simulation, componentOwner.getBody().getSpeed());
    }

    @Override
    protected AbstractDeepCloneable deepCloneHelper(
            Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
        return new Builder().fromClone(this, mapDict).build();
    }

    @Override
    public void initialize(Simulation simulation) {
        super.initialize(simulation);
        movementAcutator = componentOwner.getInterface(Movement2DAcutator.class);
    }

    @Override
    public void export(Exporter e) {
        super.export(e);
        e.addField(new ValueAdaptor<Double>("", Double.class, speed) {

            @Override
            protected void writeThrough(Double arg0) {
                speed = arg0;
            }
        });
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
        private double speed;

        public T speed(double speed) { this.speed = speed; return self(); }

        protected T fromClone(RandomMovementAction action, Map<AbstractDeepCloneable, AbstractDeepCloneable> mapDict) {
            super.fromClone(action, mapDict).speed(action.speed);
            return self();
        }
    }
}
