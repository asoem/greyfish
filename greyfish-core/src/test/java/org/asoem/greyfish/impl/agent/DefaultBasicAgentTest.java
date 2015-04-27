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

package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.actions.AbstractAgentAction;
import org.asoem.greyfish.core.actions.ActionExecutionResult;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.agent.AgentType;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;

public class DefaultBasicAgentTest {

    @Test
    public void testBuilder() throws Exception {
        // given
        final AgentAction<Object> action = new AbstractAgentAction<Object>("test action") {
            @Override
            public ActionExecutionResult apply(final Object context) {
                return ActionExecutionResult.BREAK;
            }
        };
        final AgentType agentTypeMock = mock(AgentType.class);
        final DefaultBasicAgent.Builder builder = DefaultBasicAgent.builder()
                .setType(agentTypeMock)
                .addAction(action);

        // when
        final DefaultBasicAgent agent = builder
                .build();

        // then
        assertThat(agent, is(notNullValue()));
        assertThat(agent.getActions(), contains((Object) action));
        assertThat(agent.getType(), is(equalTo(agentTypeMock)));
    }
}
