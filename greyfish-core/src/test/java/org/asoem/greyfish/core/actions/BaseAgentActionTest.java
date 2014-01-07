package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.conditions.ActionCondition;
import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class BaseAgentActionTest {

    /**
     * The tested method is final, so it's safe to test it for the abstract class.
     *
     * @throws Exception
     */
    @Test
    public void testPresenceOfAgentDuringProceed() throws Exception {

        // given
        final AtomicInteger integer = new AtomicInteger(0);
        BaseAgentAction<TestAgent, AgentContext<TestAgent>> action = new BaseAgentAction<TestAgent, AgentContext<TestAgent>>(mock(ActionCondition.class)) {
            @Override
            protected ActionState proceed(final AgentContext<TestAgent> context) {
                integer.incrementAndGet();
                return ActionState.COMPLETED;
            }
        };
        AgentContext context = mock(AgentContext.class);
        TestAgent agent = mock(TestAgent.class);
        given(context.agent()).willReturn(agent);

        // when
        action.apply(context);

        // then
        assertThat(integer.get(), is(1));
    }

    private interface TestAgent extends Agent<TestContext> {
    }

    private interface TestContext extends BasicSimulationContext<TestSimulation, TestAgent> {
    }

    private interface TestSimulation extends DiscreteTimeSimulation<TestAgent> {
    }
}
