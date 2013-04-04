package org.asoem.greyfish.core.eval;

/**
 * Unchecked exception thrown to indicate a evaluation error in the {@link Evaluator}
 * used to parallelApply a {@link GreyfishExpression} pattern.
 */
public class EvaluationException extends RuntimeException {
    public EvaluationException(String s, Throwable e) {
        super(s, e);
    }

    public EvaluationException(Throwable throwable) {
        super(throwable);
    }

    public EvaluationException(String s) {
        super(s);
    }

    public EvaluationException() {
    }
}
