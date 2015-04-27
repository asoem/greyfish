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

package org.asoem.greyfish.core.actions;

/**
 * Defined the result of an {@link AgentAction#apply(Object) action execution}.
 */
public enum ActionExecutionResult {
    /**
     * Indicates that the action should be the last in the current execution chain. All following actions won't get executed
     * this step.
     */
    BREAK,
    /**
     * Indicates that the action should be the last in the current execution chain and the next execution chain should
     * continue this this action.
     */
    CONTINUE,
    /**
     * Indicates that the next action in the execution chain should be tried.
     */
    NEXT
}
