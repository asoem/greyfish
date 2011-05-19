package org.asoem.greyfish.core.eval;

/**
 * User: christoph
 * Date: 19.05.11
 * Time: 10:47
 */
public class EvaluationException extends Exception {
    public EvaluationException(String s, Throwable e) {
        super(s, e);
    }

    public EvaluationException(Throwable throwable) {
        super(throwable);
    }
}
