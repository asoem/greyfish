package org.asoem.greyfish.core.eval;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 11:00
 */
public interface EvaluatorFactory {
    Evaluator createEvaluator(String expression) throws SyntaxException;
}
