package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.simulation.Simulation;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.AbstractTypedValueModel;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.simpleframework.xml.Element;

import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 11.11.11
 * Time: 11:42
 */
@ClassGroup(tags="conditions")
public class GreyfishExpressionCondition extends LeafCondition {

    // There is a potential performance increase.
    // Some Expressions, that for example reference a gene,
    // have functions that return a constant value over the lifetime of an Agent
    @Element
    private GreyfishExpression expression = GreyfishExpressionFactoryHolder.compile("false");


    public GreyfishExpressionCondition(GreyfishExpressionCondition greyfishExpressionCondition, DeepCloner cloner) {
        super(greyfishExpressionCondition, cloner);
        this.expression = greyfishExpressionCondition.expression;
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public GreyfishExpressionCondition() {
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new GreyfishExpressionCondition(this, cloner);
    }

    @Override
    public boolean evaluate(@Nullable Simulation simulation) {
        return expression.evaluateForContext(this).asBoolean();
    }

    @Override
    public void configure(ConfigurationHandler e) {
        super.configure(e);
        e.add("bool expression", new AbstractTypedValueModel<GreyfishExpression>() {
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

    public static GreyfishExpressionCondition evaluate(GreyfishExpression compile) {
        final GreyfishExpressionCondition greyfishExpressionCondition = new GreyfishExpressionCondition();
        greyfishExpressionCondition.expression = compile;
        return greyfishExpressionCondition;
    }
}
