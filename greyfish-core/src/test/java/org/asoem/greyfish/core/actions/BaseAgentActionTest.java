package org.asoem.greyfish.core.actions;

import org.asoem.greyfish.core.actions.utils.ActionState;
import org.asoem.greyfish.core.agent.Agent;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.conditions.ActionCondition;
import org.asoem.greyfish.core.simulation.DiscreteTimeSimulation;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BaseAgentActionTest {

    /**
     * The tested method is final, so it's safe to test it for the abstract class.
     *
     * @throws Exception
     */
    @Test
    public void testPresenceOfAgentDuringProceed() throws Exception {

        // given
        BaseAgentAction<TestAgent> action = new BaseAgentAction<TestAgent>(mock(ActionCondition.class)) {
            @Override
            protected ActionState proceed(final ExecutionContext<TestAgent> context) {
                agent().get().hashCode();
                return ActionState.COMPLETED;
            }
        };
        ExecutionContext context = mock(ExecutionContext.class);
        TestAgent agent = mock(TestAgent.class);
        given(context.agent()).willReturn(agent);

        // when
        action.apply(context);

        // then
        verify(agent).hashCode();
    }

    private interface TestAgent extends Agent<TestAgent, TestContext> {
    }

    private interface TestContext extends BasicSimulationContext<TestSimulation, TestAgent> {
    }

    private interface TestSimulation extends DiscreteTimeSimulation<TestAgent> {
    }
}
