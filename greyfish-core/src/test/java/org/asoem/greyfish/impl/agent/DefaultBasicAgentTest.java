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
