/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.utils.base.DeepCloner;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical AND operator.
 * @author christoph
 *
 */
public class AllCondition extends BranchCondition {

    public AllCondition() {}

    @SimpleXMLConstructor
    public AllCondition(GFCondition ... conditions) {
        super(conditions);
    }

    protected AllCondition(AllCondition cloneable, DeepCloner map) {
        super(cloneable, map);
    }

    protected AllCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    @Override
    public boolean apply(final Simulation simulation) {
        switch (conditions.size()) {
            case 0 : return true;
            case 1 : return conditions.get(0).apply(simulation);
            case 2 : return conditions.get(0).apply(simulation) && conditions.get(1).apply(simulation);
            default : return Iterables.all(conditions, new Predicate<GFCondition>() {
                @Override
                public boolean apply(GFCondition condition) {
                    return condition.apply(simulation);
                }
            });
        }
    }

    @Override
    public AbstractAgentComponent deepClone(DeepCloner cloner) {
        return new AllCondition(this, cloner);
    }

    public static AllCondition all(GFCondition ... conditions) { return new AllCondition(conditions); }

    public static final class Builder extends AbstractBuilder<AllCondition, Builder> {

        public Builder(GFCondition ... conditions) {
            add(conditions);
        }

        @Override protected Builder self() { return this; }
        @Override public AllCondition checkedBuild() { return new AllCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends AllCondition, T extends AbstractBuilder<E,T>> extends BranchCondition.AbstractBuilder<E,T> {
    }
}
