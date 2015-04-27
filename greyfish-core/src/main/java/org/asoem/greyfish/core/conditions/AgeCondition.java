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
import org.asoem.greyfish.core.agent.BasicContext;
import org.asoem.greyfish.utils.base.Tagged;

@Tagged("conditions")
public class AgeCondition<A extends Agent<? extends BasicContext<?, A>>> extends LongCompareCondition<A> {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public AgeCondition() {
    }

    private AgeCondition(final AbstractBuilder<?, ?, A> builder) {
        super(builder);
    }

    @Override
    protected Long getCompareValue() {
        return agent().get().getContext().get().getAge();
    }

    public static final class Builder<A extends Agent<? extends BasicContext<?, A>>> extends AbstractBuilder<AgeCondition<A>, Builder<A>, A> {
        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected AgeCondition<A> checkedBuild() {
            return new AgeCondition<A>(this);
        }
    }

    protected static abstract class AbstractBuilder<E extends AgeCondition<A>, T extends AbstractBuilder<E, T, A>, A extends Agent<? extends BasicContext<?, A>>> extends LongCompareCondition.AbstractBuilder<E, T, A> {
    }
}
