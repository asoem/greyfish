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

import com.google.common.base.Optional;
import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentNode;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ForwardingCondition<A extends Agent<?>> extends ForwardingObject implements ActionCondition<A> {

    protected abstract ActionCondition<A> delegate();

    @Override
    public Optional<AgentAction<?>> getAction() {
        return delegate().getAction();
    }

    @Override
    public AgentAction<?> action() {
        return delegate().action();
    }

    public void setAction(final AgentAction<?> action) {
        delegate().setAction(action);
    }

    @Override
    public List<ActionCondition<A>> getChildConditions() {
        return delegate().getChildConditions();
    }

    @Override
    public ActionCondition<A> getRoot() {
        return delegate().getRoot();
    }

    public void setParent(@Nullable final ActionCondition<A> parent) {
        delegate().setParent(parent);
    }

    @Override
    public ActionCondition<A> getParent() {
        return delegate().getParent();
    }

    public void insert(final ActionCondition<A> condition, final int index) {
        delegate().insert(condition, index);
    }

    public void add(final ActionCondition<A> condition) {
        delegate().add(condition);
    }

    public void remove(final ActionCondition<A> condition) {
        delegate().remove(condition);
    }

    @Override
    public void removeAll() {
        delegate().removeAll();
    }

    @Override
    public boolean isLeafCondition() {
        return delegate().isLeafCondition();
    }

    @Override
    public boolean isRootCondition() {
        return delegate().isRootCondition();
    }

    @Override
    public boolean evaluate() {
        return delegate().evaluate();
    }

    @Override
    public Optional<A> agent() throws IllegalStateException {
        return delegate().agent();
    }

    public void setAgent(@Nullable final A agent) {
        delegate().setAgent(agent);
    }

    @Override
    public String getName() {
        return delegate().getName();
    }

    @Override
    public void initialize() {
        delegate().initialize();
    }

    @Override
    public Iterable<AgentNode> children() {
        return delegate().children();
    }

}
