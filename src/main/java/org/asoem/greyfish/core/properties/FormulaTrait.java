package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.io.Logger;
import org.asoem.greyfish.core.io.LoggerFactory;
import org.asoem.greyfish.lang.BuilderInterface;
import org.asoem.greyfish.lang.ClassGroup;
import org.asoem.greyfish.utils.ConfigurationHandler;
import org.asoem.greyfish.utils.DeepCloneable;
import org.asoem.greyfish.utils.DeepCloner;
import org.asoem.greyfish.utils.ValueAdaptor;

import static org.asoem.greyfish.core.eval.GreyfishExpressionFactory.compileExpression;

/**
 * User: christoph
 * Date: 05.09.11
 * Time: 18:25
 */
@ClassGroup(tags = {"property"})
public class FormulaTrait extends AbstractGFProperty implements DiscreteProperty<Double> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormulaTrait.class);

    private GreyfishExpression<FormulaTrait> expression = compileExpression("0").forContext(FormulaTrait.class);

    protected FormulaTrait(AbstractBuilder<? extends AbstractBuilder> builder) {
        super(builder);
        this.expression = builder.expression;
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
        e.add(ValueAdaptor.forField("expression", GreyfishExpression.class, this, "Formula"));
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

    public static class Builder extends AbstractBuilder<Builder> implements BuilderInterface<FormulaTrait> {

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public FormulaTrait build() {
            return new FormulaTrait(checkedSelf());
        }
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder<T>> extends AbstractGFProperty.AbstractBuilder<T> {
        private GreyfishExpression<FormulaTrait> expression;

        public T expression(String expression) { this.expression = compileExpression(expression).forContext(FormulaTrait.class); return self(); }
    }
}
