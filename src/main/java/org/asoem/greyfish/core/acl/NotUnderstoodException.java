package org.asoem.greyfish.core.acl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 14.01.11
 * Time: 14:19
 */
public class NotUnderstoodException extends RuntimeException {
    public NotUnderstoodException() {
    }

    public NotUnderstoodException(String message) {
        super(message);
    }

    public NotUnderstoodException(String formattedMessage, Object ... args) {
        super(String.format(formattedMessage, args));
    }

    public NotUnderstoodException(Throwable cause) {
        super(cause);
    }

    public static NotUnderstoodException unexpectedPayloadType(ACLMessage message, Class<?> clazz) {
        checkNotNull(message);
        Class<?> clazzReceived = message.getContentClass();
        return new NotUnderstoodException("Unexpected Payload Type: Received %s while expect was %s", (clazzReceived==null) ? null : clazzReceived, clazz);
    }
}
