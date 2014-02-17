package org.asoem.greyfish.core.eval;


public interface EvaluatorFactory {
    Evaluator createEvaluator(String expression);
}
