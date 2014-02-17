package org.asoem.greyfish.core.eval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractExpression implements Expression {
    private static final Logger logger = LoggerFactory.getLogger(AbstractExpression.class);

    @Override
    public final EvaluationResult evaluate(final VariableResolver resolver) {
        final EvaluationResult result = getEvaluator().evaluate(resolver);
        logger.debug("{} got evaluated to {} with resolver {}", getExpression(), result, resolver);
        return result;
    }

    @Override
    public final EvaluationResult evaluate() {
        return evaluate(VariableResolvers.emptyResolver());
    }

    @Override
    public final String toString() {
        return getClass().getSimpleName() + "{" + getEvaluator() + " evaluating '" + getExpression() + "'}";
    }
}

