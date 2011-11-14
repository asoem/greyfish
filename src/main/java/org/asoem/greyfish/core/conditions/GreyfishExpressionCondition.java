package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.SingletonGreyfishExpressionFactory;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.ValueAdaptor;
import org.simpleframework.xml.Element;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 11.11.11
 * Time: 11:42
 */
@ClassGroup(tags="condition")
public class GreyfishExpressionCondition extends LeafCondition {

    @Element
    private GreyfishExpression expression = SingletonGreyfishExpressionFactory.compileExpression("false");

    public GreyfishExpressionCondition(GreyfishExpressionCondition greyfishExpressionCondition, DeepCloner cloner) {
        super(greyfishExpressionCondition, cloner);
        this.expression = greyfishExpressionCondition.expression;
    }

    public GreyfishExpressionCondition() {
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new GreyfishExpressionCondition(this, cloner);
    }

    @Override
    public boolean apply(@Nullable Simulation simulation) {
        return expression.evaluateAsBoolean(this);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("bool expression", new ValueAdaptor<GreyfishExpression>(GreyfishExpression.class) {
            @Override
            protected void set(GreyfishExpression arg0) {
                expression = arg0;
            }

            @Override
            public GreyfishExpression get() {
                return expression;
            }
        });
    }
}
