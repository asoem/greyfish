package org.asoem.greyfish.core.eval;

/**
 * Unchecked exception thrown to indicate a evaluation error in the {@link Evaluator}
 * used to evaluate an {@link Expression}.
 */
public class EvaluationException extends RuntimeException {
    public EvaluationException(final String s, final Throwable e) {
        super(s, e);
    }

    public EvaluationException(final Throwable throwable) {
        super(throwable);
    }

    public EvaluationException(final String s) {
        super(s);
    }

    public EvaluationException() {
    }
}
