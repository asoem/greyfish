/**
 * 
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloner;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical OR operator.
 * @author christoph
 *
 */
@ClassGroup(tags="conditions")
public class AnyCondition extends BranchCondition {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public AnyCondition() {
        this(new Builder());
    }

    protected AnyCondition(AbstractBuilder<?,?> builder) {
        super(builder);
    }

    public AnyCondition(AnyCondition condition, DeepCloner map) {
        super(condition, map);
    }

    @Override
    public boolean apply(final Simulation simulation) {
        switch (conditions.size()) {
            case 0 : return true;
            case 1 : return conditions.get(0).apply(simulation);
            case 2 : return conditions.get(0).apply(simulation) || conditions.get(1).apply(simulation);
            default : return Iterables.any(conditions, new Predicate<GFCondition>() {
                @Override
                public boolean apply(GFCondition condition) {
                    return condition.apply(simulation);
                }
            });
        }
    }

    @Override
    public AnyCondition deepClone(DeepCloner cloner) {
        return new AnyCondition(this, cloner);
    }

    public static final class Builder extends AbstractBuilder<AnyCondition,Builder> {
        @Override protected Builder self() { return this; }
        @Override public AnyCondition checkedBuild() { return new AnyCondition(this); }
    }
}
