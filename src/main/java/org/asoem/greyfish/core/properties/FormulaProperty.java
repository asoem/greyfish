package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactory;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;

/**
 * User: christoph
 * Date: 05.09.11
 * Time: 18:25
 */
@ClassGroup(tags = {"properties"})
public class FormulaProperty extends AbstractGFProperty implements DiscreteProperty<Double> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormulaProperty.class);

    private final GreyfishExpression expression;

    @SimpleXMLConstructor
    public FormulaProperty() {
       this(new Builder());
    }

    protected FormulaProperty(AbstractBuilder<? extends FormulaProperty, ? extends AbstractBuilder> builder) {
        super(builder);
        this.expression = builder.expression;
    }

    protected FormulaProperty(FormulaProperty cloneable, DeepCloner map) {
        super(cloneable, map);
        this.expression = cloneable.expression;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new FormulaProperty(this, cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        e.add("Formula", TypedValueModels.forField("expression", this, GreyfishExpression.class));
    }

    @Override
    public Double get() {
        try {
            return expression.evaluateForContext(this).asDouble();
        } catch (EvaluationException e) {
            LOGGER.warn("expression could not be evaluated. Returning 0.0");
            return 0.0;
        }
    }

    public static class Builder extends AbstractBuilder<FormulaProperty, Builder> {

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public FormulaProperty checkedBuild() {
            return new FormulaProperty(this);
        }
    }

    protected static abstract class AbstractBuilder<E extends FormulaProperty, T extends AbstractBuilder<E,T>> extends AbstractGFProperty.AbstractBuilder<E,T> {
        private GreyfishExpression expression = GreyfishExpressionFactory.compile("0");

        public T expression(String expression) { this.expression = GreyfishExpressionFactory.compile(expression); return self(); }
    }
}
