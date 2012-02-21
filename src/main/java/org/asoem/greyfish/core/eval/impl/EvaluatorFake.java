package org.asoem.greyfish.core.eval.impl;

import org.asoem.greyfish.core.eval.*;

/**
* User: christoph
* Date: 21.02.12
* Time: 16:31
*/
public class EvaluatorFake implements Evaluator {

    private final String EQUALIZER = "";

    @Override
    public EvaluationResult evaluate() throws EvaluationException {
        return null;
    }

    @Override
    public void setExpression(String expression) throws SyntaxException {
    }

    @Override
    public void setResolver(VariableResolver resolver) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EvaluatorFake that = (EvaluatorFake) o;

        return EQUALIZER.equals(that.EQUALIZER);
    }

    @Override
    public int hashCode() {
        return EQUALIZER.hashCode();
    }
}
