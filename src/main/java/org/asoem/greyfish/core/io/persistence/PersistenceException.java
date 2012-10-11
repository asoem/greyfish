package org.asoem.greyfish.core.io.persistence;

/**
 * User: christoph
 * Date: 10.10.12
 * Time: 21:16
 */
public class PersistenceException extends Exception {
    public PersistenceException() {
    }

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public PersistenceException(Throwable cause) {
        super(cause);
    }
}
