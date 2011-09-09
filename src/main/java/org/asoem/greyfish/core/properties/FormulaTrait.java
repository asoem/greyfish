package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishMathExpression;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.CloneMap;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.ValueAdaptor;

/**
 * User: christoph
 * Date: 05.09.11
 * Time: 18:25
 */
@ClassGroup(tags = {"property"})
public class FormulaTrait extends AbstractGFProperty implements DiscreteProperty<Double> {

    private String expression = "0";

    protected FormulaTrait(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected FormulaTrait(FormulaTrait cloneable, CloneMap map) {
        super(cloneable, map);
    }

    @Override
    public DeepCloneable deepCloneHelper(CloneMap map) {
        return new FormulaTrait(this, map);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        e.add(ValueAdaptor.forField("expression", String.class, this, "Formula"));
    }

    @Override
    public Double get() {
        try {
            return GreyfishMathExpression.evaluateAsDouble(expression,
                    agent,
                    agent.getSimulation());
        } catch (EvaluationException e) {
            LoggerFactory.getLogger(FormulaTrait.class).warn("expression could not be evaluated. Returning 0.0");
            return 0.0;
        }
    }
}
