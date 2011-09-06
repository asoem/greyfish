/**
 * 
 */
package org.asoem.greyfish.core.conditions;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.asoem.greyfish.core.actions.ActionContext;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.utils.CloneMap;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical OR operator.
 * @author christoph
 *
 */
public class AnyCondition extends LogicalOperatorCondition {

    public AnyCondition(AnyCondition condition, CloneMap map) {
        super(condition, map);
    }

    @Override
    public boolean evaluate(final ActionContext context) {
        switch (conditions.size()) {
            case 0 : return true;
            case 1 : return conditions.get(0).evaluate(context);
            case 2 : return conditions.get(0).evaluate(context) || conditions.get(1).evaluate(context);
            default : return Iterables.any(conditions, new Predicate<GFCondition>() {
                @Override
                public boolean apply(GFCondition condition) {
                    return condition.evaluate(context);
                }
            });
        }
    }

    @Override
    public AnyCondition deepCloneHelper(CloneMap map) {
        return new AnyCondition(this, map);
    }

    @SimpleXMLConstructor
    private AnyCondition() {
        this(new Builder());
    }

    protected AnyCondition(AbstractBuilder<?> builder) {
        super(builder);
    }

    public static Builder trueIf() { return new Builder(); }
    public static final class Builder extends AbstractBuilder<Builder> implements BuilderInterface<AnyCondition> {
        private Builder() {}

        @Override protected Builder self() { return this; }
        @Override public AnyCondition build() { return new AnyCondition(this); }
        public Builder any(GFCondition ... conditions) { return super.addConditions(conditions); }
    }
}
