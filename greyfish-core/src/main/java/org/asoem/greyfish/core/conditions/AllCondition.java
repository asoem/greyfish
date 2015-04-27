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

/**
 *
 */
package org.asoem.greyfish.core.conditions;

import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.Tagged;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * This class can be used to concatenate two or more <code>Condition</code> implementations with a logical AND
 * operator.
 *
 * @author christoph
 */
@Tagged("conditions")
public class AllCondition<A extends Agent<?>> extends BranchCondition<A> {

    private AllCondition(final Builder<A> builder) {
        super(builder);
    }

    @Override
    public boolean evaluate() {
        for (final ActionCondition<A> condition : getChildConditions()) {
            if (!condition.evaluate()) {
                return false;
            }
        }
        return true;
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends Agent<?>> AllCondition<A> evaluates(final ActionCondition<A> condition) {
        return new Builder<A>().add(condition).build();
    }

    public static <A extends Agent<?>> AllCondition<A> evaluates(final ActionCondition<A> condition1, final ActionCondition<A> condition2) {
        return new Builder<A>().add(condition1).add(condition2).build();
    }

    public static <A extends Agent<?>> AllCondition<A> evaluates(final ActionCondition<A> condition1, final ActionCondition<A> condition2, final ActionCondition<A> condition3) {
        return new Builder<A>().add(condition1).add(condition2).add(condition3).build();
    }

    public static <A extends Agent<?>> AllCondition<A> evaluates(final ActionCondition<A>... conditions) {
        return new Builder<A>().add(conditions).build();
    }

    public static <A extends Agent<?>> Builder<A> builder() {
        return new Builder<A>();
    }

    private static final class Builder<A extends Agent<?>> extends BranchCondition.AbstractBuilder<A, AllCondition<A>, Builder<A>> implements Serializable {
        private Builder() {
        }

        private Builder(final AllCondition<A> allCondition) {
            super(allCondition);
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected AllCondition<A> checkedBuild() {
            return new AllCondition<A>(this);
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return build();
            } catch (IllegalStateException e) {
                throw new InvalidObjectException("Build failed with: " + e.getMessage());
            }
        }

        private static final long serialVersionUID = 0;
    }
}
