package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.eval.EvaluationResult;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.gui.utils.ClassGroup;
import org.asoem.greyfish.utils.base.DeepCloneable;
import org.asoem.greyfish.utils.base.DeepCloner;
import org.asoem.greyfish.utils.gui.ConfigurationHandler;
import org.asoem.greyfish.utils.gui.TypedValueModels;
import org.simpleframework.xml.Element;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 05.09.11
 * Time: 18:25
 */
@ClassGroup(tags = {"properties"})
public class ExpressionProperty extends AbstractGFProperty implements DiscreteProperty<Object> {

    @Element
    private GreyfishExpression expression;

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public ExpressionProperty() {
       this(new Builder());
    }

    protected ExpressionProperty(AbstractBuilder<? extends ExpressionProperty, ? extends AbstractBuilder> builder) {
        super(builder);
        this.expression = builder.expression;
    }

    protected ExpressionProperty(ExpressionProperty cloneable, DeepCloner map) {
        super(cloneable, map);
        this.expression = cloneable.expression;
    }

    @Override
    public DeepCloneable deepClone(DeepCloner cloner) {
        return new ExpressionProperty(this, cloner);
    }

    @Override
    public void configure(ConfigurationHandler e) {
        e.add("Expression", TypedValueModels.forField("expression", this, GreyfishExpression.class));
    }

    public GreyfishExpression getExpression() {
        return expression;
    }

    public void setExpression(GreyfishExpression expression) {
        this.expression = expression;
    }

    @Override
    public Object getValue() {
        return evaluate().asObject();
    }

    public EvaluationResult evaluate() {
        return expression.evaluateForContext(this);
    }

    public static Builder with() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<ExpressionProperty, Builder> {

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        protected ExpressionProperty checkedBuild() {
            return new ExpressionProperty(this);
        }
    }

    protected static abstract class AbstractBuilder<E extends ExpressionProperty, T extends AbstractBuilder<E,T>> extends AbstractGFProperty.AbstractBuilder<E,T> {
        private GreyfishExpression expression = GreyfishExpressionFactoryHolder.compile("0");

        public T expression(GreyfishExpression expression) { this.expression = checkNotNull(expression); return self(); }
    }
}
