package org.asoem.greyfish.core.eval;

/**
 * User: christoph
 * Date: 22.02.12
 * Time: 14:22
 *
 * This {@code Expression} implementation will evaluate a given {@code Expression} without any modifications using a given {@code Evaluator}
 */
public class VanillaExpression implements Expression {

    private final String expression;
    private final Evaluator evaluator;

    public VanillaExpression(String expression, EvaluatorFactory evaluator) {
        this.expression = expression;
        this.evaluator = evaluator.createEvaluator(expression);
    }

    @Override
    public EvaluationResult evaluate(VariableResolver resolver) throws EvaluationException {
        return evaluator.evaluate(resolver);
    }

    @Override
    public EvaluationResult evaluate() throws EvaluationException {
        return evaluator.evaluate(null);
    }

    @Override
    public String getExpression() {
        return expression;
    }

    @Override
    public Evaluator getEvaluator() {
        return evaluator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VanillaExpression)) return false;

        VanillaExpression that = (VanillaExpression) o;

        return evaluator.equals(that.evaluator) && expression.equals(that.expression);

    }

    @Override
    public int hashCode() {
        int result = expression.hashCode();
        result = 31 * result + evaluator.hashCode();
        return result;
    }
}
