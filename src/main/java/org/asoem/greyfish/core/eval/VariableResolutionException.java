package org.asoem.greyfish.core.eval;

/**
 * Unchecked exception thrown to indicate a syntax error in a {@link GreyfishExpression} pattern.
 */
public class VariableResolutionException extends IllegalArgumentException {
    public VariableResolutionException() {
    }

    public VariableResolutionException(final String s) {
        super(s);
    }

    public VariableResolutionException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public VariableResolutionException(final Throwable cause) {
        super(cause);
    }
}
