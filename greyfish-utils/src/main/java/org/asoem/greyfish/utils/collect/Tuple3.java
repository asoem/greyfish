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

package org.asoem.greyfish.utils.collect;


public final class Tuple3<E1, E2, E3> implements Product3<E1, E2, E3> {

    private final E1 e1;
    private final E2 e2;
    private final E3 e3;

    private Tuple3(final E1 e1, final E2 e2, final E3 e3) {
        this.e1 = e1;
        this.e2 = e2;
        this.e3 = e3;
    }

    @Override
    public E1 first() {
        return e1;
    }

    @Override
    public E2 second() {
        return e2;
    }

    @Override
    public E3 third() {
        return e3;
    }

    public static <E1, E2, E3> Tuple3<E1, E2, E3> of(final E1 e1, final E2 e2, final E3 e3) {
        return new Tuple3<>(e1, e2, e3);
    }

    @SuppressWarnings("unchecked") // safe cast
    public static <E1, E2, E3> Tuple3<E1, E2, E3> copyOf(
            final Product3<? extends E1, ? extends E2, ? extends E3> product3) {
        if (product3 instanceof Tuple3) {
            return (Tuple3<E1, E2, E3>) product3;
        } else {
            return of(product3.first(), product3.second(), product3.third());
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tuple3)) {
            return false;
        }

        final Tuple3 tuple3 = (Tuple3) o;

        if (!e1.equals(tuple3.e1)) {
            return false;
        }
        if (!e2.equals(tuple3.e2)) {
            return false;
        }
        if (!e3.equals(tuple3.e3)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = e1.hashCode();
        result = 31 * result + e2.hashCode();
        result = 31 * result + e3.hashCode();
        return result;
    }
}
