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

/**
 * A message according to the Agent Communications Language (ACL)
 * <a href="http://www.fipa.org/repository/aclspecs.html">Specifications</a>.
 */
public interface ACLMessage<T> {

    /**
     * Get the recipients of the message
     *
     * @return a set of objects which act as the identifier of agent peers
     */
    Set<T> getRecipients();

    /**
     * Get the reply-to destinations of this message.
     *
     * @return a set of objects which act as the identifier of agent peers
     */
    Set<T> getAllReplyTo();

    /**
     * Get the sender of this message. It is possible to omit the sender parameter if, for example, the agent sending
     * the ACL message wishes to remain anonymous.
     *
     * @return the sender of the message
     */
    @Nullable
    T getSender();

    ACLPerformative getPerformative();

    String getReplyWith();

    @Nullable
    String getInReplyTo();

    @Nullable
    String getEncoding();

    @Nullable
    String getLanguage();

    @Nullable
    String getOntology();

    @Nullable
    String getProtocol();

    int getConversationId();

    void send(ACLMessageTransmitter transmitter);

    boolean matches(MessageTemplate performative);

    <C> C userDefinedParameter(String key, Class<C> clazz);

    @Nullable
    Object getContent();
}
