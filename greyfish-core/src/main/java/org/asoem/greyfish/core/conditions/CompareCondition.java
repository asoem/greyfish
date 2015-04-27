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
import org.asoem.greyfish.utils.base.CompareOperator;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class CompareCondition<T extends Comparable<T>, A extends Agent<?>> extends LeafCondition<A> {

    protected CompareOperator compareOperator = CompareOperator.EQUAL;

    protected T value;

    protected CompareCondition() {
    }

    protected CompareCondition(final AbstractBuilder<A, ?, ?, T> builder) {
        super(builder);
        this.compareOperator = builder.compareOperator;
        this.value = builder.value;
    }

    @Override
    public boolean evaluate() {
        return compareOperator.apply(getCompareValue(), value);
    }

    protected abstract T getCompareValue();

    protected static abstract class AbstractBuilder<A extends Agent<?>, C extends CompareCondition<?, A>, T extends AbstractBuilder<A, C, T, E>, E extends Comparable<E>> extends LeafCondition.AbstractBuilder<A, C, T> {
        private CompareOperator compareOperator;
        private E value;

        public T is(final CompareOperator compareOperator) {
            this.compareOperator = checkNotNull(compareOperator);
            return self();
        }

        public T to(final E value) {
            this.value = checkNotNull(value);
            return self();
        }
    }
}
