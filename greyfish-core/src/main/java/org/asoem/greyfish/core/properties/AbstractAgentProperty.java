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

package org.asoem.greyfish.core.properties;

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.agent.AgentNode;
import org.asoem.greyfish.core.agent.PropertyValueRequest;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractAgentProperty<C, T>
        implements AgentProperty<C, T> {

    private final String name;

    protected AbstractAgentProperty(final String name) {
        this.name = checkNotNull(name);
    }

    protected AbstractAgentProperty(final AbstractBuilder<?, ?> builder) {
        name = builder.name;
    }

    @Override
    public <T> T ask(final C context, final Object message, final Class<T> replyType) {
        if (message instanceof PropertyValueRequest) {
            return replyType.cast(value(context));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public void initialize() {
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractAgentProperty that = (AbstractAgentProperty) o;

        if (!name.equals(that.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Get all children of this node. <p>This default implementation simple returns an empty list but other
     * implementations might overwrite this method, if they add nodes to the tree.</p>
     */
    @Override
    public Iterable<AgentNode> children() {
        return ImmutableList.of();
    }

    protected abstract static class AbstractBuilder<P extends AbstractAgentProperty<?, ?>, B extends AbstractBuilder<P, B>>
            extends org.asoem.greyfish.utils.base.InheritableBuilder<P, B> implements Serializable {
        private String name;

        protected AbstractBuilder() {
        }

        public final B name(final String name) {
            this.name = name;
            return self();
        }
    }
}
