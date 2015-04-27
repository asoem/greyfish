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

package org.asoem.greyfish.core.acl;

import javax.annotation.Nullable;
import java.util.Set;


public abstract class ForwardingACLMessage<T> implements ACLMessage<T> {

    protected abstract ACLMessage<T> delegate();

    @Override
    public Set<T> getRecipients() {
        return delegate().getRecipients();
    }

    @Override
    public Set<T> getAllReplyTo() {
        return delegate().getAllReplyTo();
    }

    @Override
    public T getSender() {
        return delegate().getSender();
    }

    @Override
    public ACLPerformative getPerformative() {
        return delegate().getPerformative();
    }

    @Override
    public String getReplyWith() {
        return delegate().getReplyWith();
    }

    @Override
    public String getInReplyTo() {
        return delegate().getInReplyTo();
    }

    @Override
    public String getEncoding() {
        return delegate().getEncoding();
    }

    @Override
    public String getLanguage() {
        return delegate().getLanguage();
    }

    @Override
    public String getOntology() {
        return delegate().getOntology();
    }

    @Override
    public String getProtocol() {
        return delegate().getProtocol();
    }

    @Override
    public int getConversationId() {
        return delegate().getConversationId();
    }

    @Override
    public void send(final ACLMessageTransmitter transmitter) {
        delegate().send(transmitter);
    }

    @Override
    public boolean matches(final MessageTemplate performative) {
        return delegate().matches(performative);
    }

    @Override
    public <C> C userDefinedParameter(final String key, final Class<C> clazz) {
        return delegate().userDefinedParameter(key, clazz);
    }

    @Nullable
    @Override
    public Object getContent() {
        return delegate().getContent();
    }
}
