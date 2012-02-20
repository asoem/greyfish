/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.individual.AbstractAgentComponent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical AND operator.
 * @author christoph
 *
 */
@ClassGroup(tags="conditions")
public class AllCondition extends BranchCondition {

    @SimpleXMLConstructor
    public AllCondition() {}

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

    public static AllCondition evaluates(GFCondition... conditions) { return new Builder().add(conditions).build(); }

    public static final class Builder extends AbstractBuilder<AllCondition, Builder> {

        public Builder(GFCondition ... conditions) {
            add(conditions);
        }

        @Override protected Builder self() { return this; }
        @Override protected AllCondition checkedBuild() { return new AllCondition(this); }
    }

    protected static abstract class AbstractBuilder<E extends AllCondition, T extends AbstractBuilder<E,T>> extends BranchCondition.AbstractBuilder<E,T> {
    }
}
