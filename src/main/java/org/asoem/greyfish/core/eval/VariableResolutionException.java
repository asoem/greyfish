package org.asoem.greyfish.core.eval;

/**
 * Unchecked exception thrown to indicate a syntax error in a {@link GreyfishExpression} pattern.
 */
public class VariableResolutionException extends IllegalArgumentException {
    public VariableResolutionException() {
    }

    public VariableResolutionException(String s) {
        super(s);
    }

    public VariableResolutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public VariableResolutionException(Throwable cause) {
        super(cause);
    }
}
