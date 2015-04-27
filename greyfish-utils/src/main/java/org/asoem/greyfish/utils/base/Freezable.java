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
 * This interface is intended to assist objects that are able to switch into an immutable state.
 * The implementation is asked to intercept evaluates write accessors of this object and prevent any modification from outside.
 * It also shouldn't modify itself from inside.
 */
public interface Freezable {
    /**
     * Freeze this object. If an object is frozen, it should act as if evaluates of its fields are immutable.
     */
    void freeze();

    /**
     * Ask the object if it has been frozen.
     * @return {@code true} if the object implementing this interface is frozen (has been {@link #freeze}d), {@code false} otherwise.
     */
    boolean isFrozen();
}
