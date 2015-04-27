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
import org.asoem.greyfish.utils.base.Tagged;

import static com.google.common.base.Preconditions.checkArgument;

@Tagged("conditions")
public class RandomCondition<A extends Agent<?>> extends LeafCondition<A> {

    private double probability;

    @Override
    public boolean evaluate() {
        return Math.random() < probability;
    }

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    public RandomCondition() {
        this(new Builder<A>());
    }

    private RandomCondition(final AbstractBuilder<A, ?, ?> builder) {
        super(builder);
    }

    public static final class Builder<A extends Agent<?>> extends AbstractBuilder<A, RandomCondition<A>, Builder<A>> {
        @Override
        protected Builder<A> self() {
            return this;
        }

        public RandomCondition<A> checkedBuild() {
            return new RandomCondition<A>(this);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    protected static abstract class AbstractBuilder<A extends Agent<?>, E extends RandomCondition<A>, T extends AbstractBuilder<A, E, T>> extends LeafCondition.AbstractBuilder<A, E, T> {
        private double probability;

        public T probability(final double probability) {
            checkArgument(probability >= 0 && probability <= 1, "Value is not in open interval [0,1]: " + probability);
            this.probability = probability;
            return self();
        }
    }
}
