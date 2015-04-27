/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
