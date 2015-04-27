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

package org.asoem.greyfish.core.agent;

import com.google.common.collect.ForwardingObject;
import org.asoem.greyfish.core.actions.AgentContext;

abstract class ForwardingAgent<C extends Context<?, ?>, AC extends AgentContext<?>>
        extends ForwardingObject
        implements Agent<C> {

    @Override
    protected abstract Agent<?> delegate();

    @Override
    public AgentType getType() {
        return delegate().getType();
    }

    @Override
    public void run() {
        delegate().run();
    }

    @Override
    public void deactivate() {
        delegate().deactivate();
    }

    @Override
    public boolean isActive() {
        return delegate().isActive();
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
