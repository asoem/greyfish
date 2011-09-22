package org.asoem.greyfish.core.genes;

/**
 * User: christoph
 * Date: 21.09.11
 * Time: 11:24
 */
public class IncompatibleGenomeException extends RuntimeException {
    public IncompatibleGenomeException() {
    }

    public IncompatibleGenomeException(String message) {
        super(message);
    }

    public IncompatibleGenomeException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompatibleGenomeException(Throwable cause) {
        super(cause);
    }
}
