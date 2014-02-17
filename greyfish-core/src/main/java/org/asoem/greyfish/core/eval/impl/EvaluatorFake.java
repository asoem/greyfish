package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.EvaluationResult;
import org.asoem.greyfish.core.eval.Evaluator;
import org.asoem.greyfish.core.eval.VariableResolver;

public enum EvaluatorFake implements Evaluator {
    INSTANCE;

    @Override
    public EvaluationResult evaluate(final VariableResolver resolver) {
        return new ConvertingEvaluationResult(null);
    }

    @Override
    public String getExpression() {
        return "";
    }


    @Override
    public String toString() {
        return "EvaluatorFake";
    }
}
