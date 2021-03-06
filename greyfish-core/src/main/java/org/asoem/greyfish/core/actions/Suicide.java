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

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * @param <A>
 * @deprecated Use a {@link GenericAction} to remove an agent from a getSimulation
 */
@Deprecated
public class Suicide<A extends Agent<?>> extends BaseAgentAction<A, AgentContext<A>> {

    @SuppressWarnings("UnusedDeclaration") // Needed for construction by reflection / deserialization
    private Suicide() {
        this(new Builder<A>());
    }

    private Suicide(final AbstractBuilder<A, ? extends Suicide<A>, ? extends AbstractBuilder<A, ?, ?, AgentContext<A>>, AgentContext<A>> builder) {
        super(builder);
    }

    @Override
    protected ActionState proceed(final AgentContext<A> context) {
        throw new UnsupportedOperationException("");
        /*
        agent().die();
        agent().logEvent(this, "dies", "");
        return ActionState.COMPLETED;
        */
    }

    private Object writeReplace() {
        return new Builder<A>(this);
    }

    private void readObject(final ObjectInputStream stream)
            throws InvalidObjectException {
        throw new InvalidObjectException("Builder required");
    }

    public static <A extends Agent<?>> Builder<A> builder() {
        return new Builder<A>();
    }

    public static final class Builder<A extends Agent<?>> extends AbstractBuilder<A, Suicide<A>, Builder<A>, AgentContext<A>> implements Serializable {
        private Builder() {
        }

        private Builder(final Suicide<A> suicide) {
            super(suicide);
        }

        @Override
        protected Builder<A> self() {
            return this;
        }

        @Override
        protected Suicide<A> checkedBuild() {
            return new Suicide<A>(this);
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
