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
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.AgentComponent;

import javax.annotation.Nullable;
import java.util.List;

public interface ActionCondition<A extends Agent<?>> extends AgentComponent<A> {

    Optional<AgentAction<?>> getAction();

    AgentAction<?> action();

    void setAction(AgentAction<?> action);

    List<ActionCondition<A>> getChildConditions();

    ActionCondition<A> getRoot();

    void setParent(@Nullable ActionCondition<A> parent);

    ActionCondition<A> getParent();

    void insert(ActionCondition<A> condition, int index);

    void add(ActionCondition<A> condition);

    void remove(ActionCondition<A> condition);

    void removeAll();

    boolean isLeafCondition();

    boolean isRootCondition();

    boolean evaluate();

    /**
     * Get the agent this component was added to.
     *
     * @return the agent for this component
     */
    Optional<A> agent();

    /**
     * Sets the connected agent. This method should only be called by an Agent implementation in an addXXX method.
     *
     * @param agent the new agent
     */
    void setAgent(@Nullable A agent);
}
