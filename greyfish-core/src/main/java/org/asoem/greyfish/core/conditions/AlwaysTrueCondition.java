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

public class AlwaysTrueCondition<A extends Agent<?>> extends LeafCondition<A> {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public AlwaysTrueCondition() {
        this(new Builder<A>());
    }

    private AlwaysTrueCondition(final AbstractBuilder<A, ?, ?> builder) {
        super(builder);
    }

    @Override
    public boolean evaluate() {
        return true;
    }

    public static <A extends Agent<?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public static final class Builder<A extends Agent<?>> extends AbstractBuilder<A, AlwaysTrueCondition<A>, Builder<A>> {
        private Builder() {
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        public AlwaysTrueCondition<A> checkedBuild() {
            return new AlwaysTrueCondition<A>(this);
        }
    }

    protected static abstract class AbstractBuilder<A extends Agent<?>, E extends AlwaysTrueCondition<A>, T extends AbstractBuilder<A, E, T>> extends LeafCondition.AbstractBuilder<A, E, T> {
    }
}
