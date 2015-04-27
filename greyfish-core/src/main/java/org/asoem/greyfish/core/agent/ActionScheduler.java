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

package org.asoem.greyfish.core.agent;

/**
 * An {@code ActionScheduler} defines how instances of {@code AgentAction} get executed by the {@code Agent} and logs
 * the execution history.
 */
public interface ActionScheduler<C> {
    /**
     * Execute the next action.
     *
     * @param context the context for the actions
     * @return {@code true} if an action got executed, {@code false} otherwise.
     */
    boolean executeNext(C context);

    /**
     * Reset the history.
     */
    void reset();
}
