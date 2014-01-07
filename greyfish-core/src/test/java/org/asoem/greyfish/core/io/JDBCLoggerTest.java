package org.asoem.greyfish.core.io;

import com.google.common.util.concurrent.MoreExecutors;
import org.asoem.greyfish.core.agent.BasicSimulationContext;
import org.asoem.greyfish.core.agent.PrototypeGroup;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.simulation.SpatialSimulation2D;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class JDBCLoggerTest {
    @Test
    public void testCommitThreshold1() throws Exception {
        // given
        final int commitThreshold = 1;
        final Connection connectionMock = mock(Connection.class);
        given(connectionMock.prepareStatement(any(String.class))).willReturn(mock(PreparedStatement.class));
        final ConnectionManager mock = when(mock(ConnectionManager.class).get()).thenReturn(connectionMock).getMock();
        final JDBCLogger<TestAgent> jdbcLogger = new JDBCLogger<>(mock, commitThreshold);
        final TestAgent agentMock = mock(TestAgent.class);
        when(agentMock.getPrototypeGroup()).thenReturn(PrototypeGroup.named(""));
        //when(agentMock.getTraits()).thenReturn(ImmutableFunctionalList.<AgentTrait<?, ?>>of());
        final TestSimulation simulation2D = mock(TestSimulation.class);
        when(simulation2D.getName()).thenReturn("testSimulation");
        final BasicSimulationContext<TestSimulation, TestAgent> contextMock = mock(BasicSimulationContext.class);
        //given(agentMock.getContext()).willReturn(Optional.of(contextMock));
        when(contextMock.getSimulation()).thenReturn(simulation2D);
        when(contextMock.simulationName()).thenReturn("testSimulation");

        // when
        jdbcLogger.logAgentCreation(agentMock); // two commits
        jdbcLogger.logAgentCreation(agentMock); // one commit
        MoreExecutors.sameThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new AssertionError(e);
                    }
                } while (!jdbcLogger.queries.isEmpty());
            }
        }).get(1, TimeUnit.SECONDS);


        // then
        verify(connectionMock, times(3)).commit();
    }

    @Test
    public void testCommitThreshold3() throws Exception {
        // given
        final int commitThreshold = 3;
        final Connection connectionMock = mock(Connection.class);
        given(connectionMock.prepareStatement(any(String.class))).willReturn(mock(PreparedStatement.class));
        final ConnectionManager mock = when(mock(ConnectionManager.class).get()).thenReturn(connectionMock).getMock();
        SimulationLogger<TestAgent> jdbcLogger = SimulationLoggers.createJDBCLogger(mock, commitThreshold);
        final TestAgent agentMock = mock(TestAgent.class);
        when(agentMock.getPrototypeGroup()).thenReturn(PrototypeGroup.named(""));
        //when(agentMock.getTraits()).thenReturn(ImmutableFunctionalList.<AgentTrait<?, ?>>of());
        final TestSimulation simulation2D = mock(TestSimulation.class);
        when(simulation2D.getName()).thenReturn("");
        final BasicSimulationContext<TestSimulation, TestAgent> contextMock = mock(BasicSimulationContext.class);
        //given(agentMock.getContext()).willReturn(Optional.of(contextMock));
        when(contextMock.getSimulation()).thenReturn(simulation2D);
        when(contextMock.simulationName()).thenReturn("testSimulation");

        // when
        jdbcLogger.logAgentCreation(agentMock); // two commits
        jdbcLogger.logAgentCreation(agentMock); // one commit
        Thread.sleep(10);

        // then
        verify(connectionMock, times(1)).commit();
    }

    private interface TestSimulation extends SpatialSimulation2D<TestAgent, Space2D<TestAgent, Object2D>> {
    }

    private interface TestAgent extends SpatialAgent<TestAgent, TestSimulationContext, Object2D> {
    }

    private interface TestSimulationContext extends BasicSimulationContext<TestSimulation, TestAgent> {}
}
