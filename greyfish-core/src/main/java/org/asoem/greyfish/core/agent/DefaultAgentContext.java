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

import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.core.acl.ACLMessage;
import org.asoem.greyfish.core.acl.MessageTemplate;
import org.asoem.greyfish.core.actions.AgentContext;

public final class DefaultAgentContext<A extends Agent<?>> implements AgentContext<A> {
    private A agent;
    private final BasicContext<?, A> simulationContext;

    public DefaultAgentContext(final A agent, final BasicContext<?, A> simulationContext) {
        this.agent = agent;
        this.simulationContext = simulationContext;
    }

    @Override
    public A agent() {
        return agent;
    }

    @Override
    public Iterable<A> getActiveAgents() {
        return ImmutableList.copyOf(simulationContext.getActiveAgents());
    }

    @Override
    public void receive(final ACLMessage<A> message) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Iterable<ACLMessage<A>> getMessages(final MessageTemplate template) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void sendMessage(final ACLMessage<A> message) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
