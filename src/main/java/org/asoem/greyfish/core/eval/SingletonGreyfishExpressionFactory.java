package org.asoem.greyfish.core.eval;

/**
 * User: christoph
 * Date: 14.09.11
 * Time: 12:02
 */
public enum SingletonGreyfishExpressionFactory implements GreyfishExpressionFactory {
    INSTANCE;

    private EvaluatorFactory evaluatorFactory = new EvaluatorFactory() {
        @Override
        public Evaluator createEvaluator(String expression) throws SyntaxException {
            return new MvelEvaluator(expression);
        }
    };

    @Override
    public GreyfishExpression create(String expression) throws SyntaxException {
        Evaluator evaluator = evaluatorFactory.createEvaluator(expression);
        return new GreyfishExpression(expression, evaluator);
    }

    public static GreyfishExpression compileExpression(String expression) {
        return INSTANCE.create(expression);
    }
}
