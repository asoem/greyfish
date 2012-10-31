package org.asoem.greyfish.core.eval;

import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;

/**
 * User: christoph
 * Date: 31.10.12
 * Time: 10:16
 */
public abstract class AbstractExpression implements Expression {
    private static final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(AbstractExpression.class);

    @Override
    public EvaluationResult evaluate(VariableResolver resolver) throws EvaluationException {
        final EvaluationResult result = getEvaluator().evaluate(resolver);
        LOGGER.debug("{} got evaluated to {} with resolver {}", getExpression(), result, resolver);
        return result;
    }

    @Override
    public EvaluationResult evaluate() throws EvaluationException {
        return evaluate(VariableResolvers.emptyResolver());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"+ getEvaluator() + " evaluating '" + getExpression() + "'}";
    }
}
