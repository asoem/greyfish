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

import com.google.common.base.Objects;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;

import java.util.Collections;
import java.util.List;

public abstract class LeafCondition<A extends Agent<?>> extends AbstractCondition<A> {

    protected LeafCondition() {
    }

    protected LeafCondition(final AbstractBuilder<A, ?, ?> builder) {
        super(builder);
    }

    @Override
    public final List<ActionCondition<A>> getChildConditions() {
        return Collections.emptyList();
    }

    @Override
    public final boolean isLeafCondition() {
        return true;
    }

    @Override
    public final Iterable<AgentNode> children() {
        return Collections.emptyList();
    }

    @Override
    public final void remove(final ActionCondition<A> condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void removeAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void add(final ActionCondition<A> condition) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(final ActionCondition<A> condition, final int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return (isRootCondition() ? "*" : "") + Objects.toStringHelper(this).toString();
    }

    protected static abstract class AbstractBuilder<A extends Agent<?>, C extends AbstractCondition<A>, B extends AbstractBuilder<A, C, B>> extends AbstractCondition.AbstractBuilder<A, C, B> {
        protected AbstractBuilder(final LeafCondition<A> leafCondition) {
            super(leafCondition);
        }

        protected AbstractBuilder() {
        }
    }
}
