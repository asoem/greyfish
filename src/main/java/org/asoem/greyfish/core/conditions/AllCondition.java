/**
 *
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.ActionContext;
import org.asoem.greyfish.core.individual.AbstractGFComponent;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical AND operator.
 * @author christoph
 *
 */
public class AllCondition extends LogicalOperatorCondition {

    @Override
    public AbstractGFComponent deepCloneHelper(CloneMap map) {
        return new AllCondition(this, map);
    }

    @SimpleXMLConstructor
    private AllCondition() {
        this(new Builder());
    }

    protected AllCondition(AllCondition cloneable, CloneMap map) {
        super(cloneable, map);
    }

    protected AllCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    @Override
    public boolean evaluate(final ActionContext context) {
        switch (conditions.size()) {
            case 0 : return true;
            case 1 : return conditions.get(0).evaluate(context);
            case 2 : return conditions.get(0).evaluate(context) && conditions.get(1).evaluate(context);
            default : return Iterables.all(conditions, new Predicate<GFCondition>() {
                @Override
                public boolean apply(GFCondition condition) {
                    return condition.evaluate(context);
                }
            });
        }
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<AllCondition> {
        private Builder() {}
        @Override protected Builder self() { return this; }
        @Override public AllCondition build() { return new AllCondition(this); }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends LogicalOperatorCondition.AbstractBuilder<T> {
        public T and(GFCondition ... conditions) { return addConditions(conditions); }
        public T all(Iterable<GFCondition> conditions) { return addConditions(conditions); }
    }
}
