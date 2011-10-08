/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.simulation.ParallelizedSimulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.DeepCloner;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical AND operator.
 * @author christoph
 *
 */
public class AllCondition extends LogicalOperatorCondition {


    @Override
    public AbstractAgentComponent deepClone(DeepCloner cloner) {
        return new AllCondition(this, cloner);
    }

    @SimpleXMLConstructor
    public AllCondition(GFCondition ... conditions) {
        super(conditions);
    }

    protected AllCondition(AllCondition cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    protected AllCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    @Override
    public boolean evaluate(final ParallelizedSimulation simulation) {
        switch (conditions.size()) {
            case 0 : return true;
            case 1 : return conditions.get(0).evaluate(simulation);
            case 2 : return conditions.get(0).evaluate(simulation) && conditions.get(1).evaluate(simulation);
            default : return Iterables.all(conditions, new Predicate<GFCondition>() {
                @Override
                public boolean apply(GFCondition condition) {
                    return condition.evaluate(simulation);
                }
            });
        }
    }

    public static AllCondition all(GFCondition ... conditions) { return new AllCondition(conditions); }

    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<AllCondition> {

        public Builder(GFCondition ... conditions) {
            add(conditions);
        }

        @Override protected Builder self() { return this; }
        @Override public AllCondition build() { return new AllCondition(checkedSelf()); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LogicalOperatorCondition.AbstractBuilder<T> {
    }
}
