package org.asoem.greyfish.core.properties;

import org.asoem.greyfish.core.eval.EvaluationResult;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.eval.GreyfishExpressionFactoryHolder;
import org.asoem.greyfish.core.utils.SimpleXMLConstructor;
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
public class ExpressionProperty extends AbstractGFProperty implements DiscreteProperty<EvaluationResult> {

    @Element
    private GreyfishExpression expression;

    @SimpleXMLConstructor
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
    public EvaluationResult get() {
        return expression.evaluateForContext(this);
    }

    public static class Builder extends AbstractBuilder<ExpressionProperty, Builder> {

        @Override
        protected Builder self() {
            return this;
        }

        @Override
        public ExpressionProperty checkedBuild() {
            return new ExpressionProperty(this);
        }
    }

    protected static abstract class AbstractBuilder<E extends ExpressionProperty, T extends AbstractBuilder<E,T>> extends AbstractGFProperty.AbstractBuilder<E,T> {
        private GreyfishExpression expression = GreyfishExpressionFactoryHolder.compile("0");

        public T expression(GreyfishExpression expression) { this.expression = checkNotNull(expression); return self(); }
    }
}
