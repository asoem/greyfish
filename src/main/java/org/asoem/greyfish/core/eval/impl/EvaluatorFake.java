package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.EvaluationException;
import org.asoem.greyfish.core.eval.EvaluationResult;
import org.asoem.greyfish.core.eval.Evaluator;
import org.asoem.greyfish.core.eval.VariableResolver;

/**
* User: christoph
* Date: 21.02.12
* Time: 16:31
*/
public enum EvaluatorFake implements Evaluator {
    INSTANCE;

    @Override
    public EvaluationResult evaluate(final VariableResolver resolver) throws EvaluationException {
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
