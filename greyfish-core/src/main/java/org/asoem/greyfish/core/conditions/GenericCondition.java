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

import com.google.common.collect.ImmutableMap;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.utils.base.Callback;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


public class GenericCondition<A extends Agent<?>> extends LeafCondition<A> implements Serializable {

    private final Callback<? super GenericCondition<A>, Boolean> callback;

    private GenericCondition(final Builder<A> builder) {
        super(builder);
        this.callback = builder.callback;
    }

    @Override
    public boolean evaluate() {
        return callback.apply(this, ImmutableMap.<String, Object>of());
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public Callback<? super GenericCondition<A>, Boolean> getCallback() {
        return callback;
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends Agent<?>> GenericCondition<A> evaluate(final Callback<? super GenericCondition<A>, Boolean> callback) {
        return new Builder<A>().callback(callback).build();
    }

    public static <A extends Agent<?>> Builder<A> builder() {
        return new Builder<A>();
    }

    private static final class Builder<A extends Agent<?>> extends LeafCondition.AbstractBuilder<A, GenericCondition<A>, Builder<A>> implements Serializable {
        public Callback<? super GenericCondition<A>, Boolean> callback;

        private Builder() {
        }

        private Builder(final GenericCondition<A> genericCondition) {
            super(genericCondition);
            this.callback = genericCondition.callback;
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected GenericCondition<A> checkedBuild() {
            checkState(callback != null, "Cannot build without a callback");
            return new GenericCondition<A>(this);
        }

        public Builder<A> callback(final Callback<? super GenericCondition<A>, Boolean> callback) {
            this.callback = checkNotNull(callback);
            return self();
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
