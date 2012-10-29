package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.*;

/**
* User: christoph
* Date: 21.02.12
* Time: 16:31
*/
public enum EvaluatorFake implements Evaluator {
    INSTANCE;

    @Override
    public EvaluationResult evaluate(VariableResolver resolver) throws EvaluationException {
        return null;
    }

    @Override
    public String getExpression() {
        return "";
    }

    @Override
    public void setExpression(String expression) throws SyntaxException {
    }

}
