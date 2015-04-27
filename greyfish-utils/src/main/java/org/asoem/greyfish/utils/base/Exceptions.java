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

package org.asoem.greyfish.utils.base;

/**
 * User: christoph
 * Date: 12.04.12
 * Time: 14:44
 *
 * Taken from http://www.eishay.com/2011/11/throw-undeclared-checked-exception-in.html
 */
public final class Exceptions {

    private Exceptions() {}

    public static RuntimeException asRuntimeException(final Exception e) {
        Exceptions.<RuntimeException>throwAs(e);
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAs(final Throwable e) throws E {
        throw (E)e;
    }
}
