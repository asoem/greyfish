package org.asoem.greyfish.core.eval;

/**
 * Unchecked exception thrown to indicate a syntax error in a {@link GreyfishExpression} pattern.
 */
public class SyntaxException extends IllegalArgumentException {

    public SyntaxException() {
    }

    public SyntaxException(String s) {
        super(s);
    }

    public SyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyntaxException(Throwable cause) {
        super(cause);
    }
}
