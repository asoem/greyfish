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

package org.asoem.greyfish.core.utils;

/**
 * A listener for time events.
 */
public interface DiscreteTimeListener {
    /**
     * Called when the time of the {@code source} has changed from {@code oldTime} to {@code newTime}.
     * @param source the source of the event
     * @param oldTime the old time
     * @param newTime the new time
     */
    void timeChanged(DiscreteTime source, long oldTime, long newTime);
}
