package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.DeepCloner;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(FormulaTrait.class);

    private GreyfishExpression<FormulaTrait> expression =
            GreyfishExpressionFactory.compileExpression("0").forContext(FormulaTrait.class);

    protected FormulaTrait(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
    }

    protected FormulaTrait(FormulaTrait cloneable, DeepCloner map) {
        super(cloneable, map);
        this.expression = cloneable.expression;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new FormulaTrait(this, cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        e.add(ValueAdaptor.forField("expression", String.class, this, "Formula"));
    }

    @Override
    public Double get() {
        try {
            return expression.evaluateAsDouble(this);
        } catch (EvaluationException e) {
            LOGGER.warn("expression could not be evaluated. Returning 0.0");
            return 0.0;
        }
    }
}
