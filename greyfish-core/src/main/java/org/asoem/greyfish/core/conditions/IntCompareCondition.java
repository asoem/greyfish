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

package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;

public abstract class IntCompareCondition<A extends Agent<?>> extends CompareCondition<Integer, A> {

    protected IntCompareCondition() {
    }

    protected IntCompareCondition(final AbstractBuilder<?, ?, A> builder) {
        super(builder);
    }

    protected static abstract class AbstractBuilder<E extends IntCompareCondition<A>, T extends AbstractBuilder<E, T, A>, A extends Agent<?>> extends CompareCondition.AbstractBuilder<A, E, T, Integer> {
    }
}
