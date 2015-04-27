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

import com.google.common.base.Optional;

/**
 * An Agent which is the basic unit of a {@link org.asoem.greyfish.core.environment.DiscreteTimeEnvironment}.
 *
 * @param <C> The type of the simulation context
 */
public interface Agent<C extends Context<?, ?>>
        extends AgentNode, Runnable {

    /**
     * Get the type of this agent.
     *
     * @return the type of this agent.
     */
    AgentType getType();

    /**
     * Let the agent execute it's next action
     */
    @Override
    void run();

    /*
     * Activate this agent and set the current context to {@code context}.
     *
     * @param context the new context for this agent
     */
    void activate(C context);

    /**
     * Deactivate this agent. <p>Deactivation will remove the current {@link BasicContext context}</p>
     */
    void deactivate();

    /**
     * Check if the agent's {@link BasicContext context} is present.
     *
     * @return {@code true} if the context is present, {@code false} if absent
     */
    boolean isActive();

    /*
     * Get the simulation context holder for this agent.
     *
     * @return the optional simulation context
     */
    Optional<C> getContext();

    /**
     * Send a message to this agent.
     *
     * @param message   the message
     * @param replyType the class to cast the reply to
     * @return the reply to given {@code message}
     * @throws java.lang.IllegalArgumentException if given {@code message} could not be handled
     */
    <T> T ask(Object message, Class<T> replyType);

    /**
     * Get the value for trait named {@code traitName}.
     *
     * @param traitName the name of the trait
     * @param valueType the class of the trait value
     * @param <T>       the type of the trait value class
     * @return the value of the trait
     * @throws java.lang.IllegalStateException if no trait with name equal to {@code traitName} could be found.
     */
    <T> T getPropertyValue(String traitName, Class<T> valueType);

}
