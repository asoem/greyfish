package org.asoem.greyfish.impl.agent;

import org.asoem.greyfish.core.actions.AbstractAgentAction;
import org.asoem.greyfish.core.actions.ActionExecutionResult;
import org.asoem.greyfish.core.actions.AgentAction;
import org.asoem.greyfish.core.actions.ComponentContext;
import org.asoem.greyfish.core.agent.PrototypeGroup;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class DefaultBasicAgentTest {

    @Test
    public void testBuilder() throws Exception {
        // given
        final PrototypeGroup prototypeGroup = mock(PrototypeGroup.class);
        final AgentAction<BasicAgent> action = new AbstractAgentAction<BasicAgent>("test action") {
            @Override
            public ActionExecutionResult apply(final ComponentContext<BasicAgent, ?> componentContext) {
                return ActionExecutionResult.BREAK;
            }
        };
        final DefaultBasicAgent.Builder builder = DefaultBasicAgent.builder(prototypeGroup)
                .addAction(action);

        // when
        final DefaultBasicAgent agent = builder
                .build();

        // then
        assertThat(agent, is(notNullValue()));
        assertThat(agent.getPrototypeGroup(), is(prototypeGroup));
        assertThat(agent.getActions(), contains(action));
    }

    @Test(expected = NullPointerException.class)
    public void testBuilderNullPopulation() throws Exception {
        // given
        final PrototypeGroup prototypeGroup = null;

        // when
        DefaultBasicAgent.builder(prototypeGroup).build();

        // then
        fail();
    }
}
