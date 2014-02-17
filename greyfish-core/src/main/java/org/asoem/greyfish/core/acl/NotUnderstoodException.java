package org.asoem.greyfish.core.acl;

import static com.google.common.base.Preconditions.checkNotNull;


public class NotUnderstoodException extends RuntimeException {
    public NotUnderstoodException() {
    }

    public NotUnderstoodException(final String message) {
        super(message);
    }

    public NotUnderstoodException(final String formattedMessage, final Object... args) {
        super(String.format(formattedMessage, args));
    }

    public NotUnderstoodException(final Throwable cause) {
        super(cause);
    }

    public static NotUnderstoodException unexpectedPayloadType(final ACLMessage<?> message, final Class<?> clazz) {
        checkNotNull(message);
        final Class<?> clazzReceived = message.getContent().getClass();
        return new NotUnderstoodException("Unexpected Payload Type: Received %s while expect was %s", (clazzReceived == null) ? null : clazzReceived, clazz);
    }
}
