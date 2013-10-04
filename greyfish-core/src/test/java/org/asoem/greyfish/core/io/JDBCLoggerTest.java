package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Population;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.utils.collect.ImmutableFunctionalList;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;

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
        SimulationLogger<SpatialAgent<?, ?, ?>> jdbcLogger = SimulationLoggers.createJDBCLogger(mock, commitThreshold);
        final SpatialAgent agentMock = mock(SpatialAgent.class);
        when(agentMock.getPopulation()).thenReturn(Population.named(""));
        when(agentMock.getTraits()).thenReturn(ImmutableFunctionalList.of());

        // when
        jdbcLogger.logAgentCreation(agentMock); // two commits
        jdbcLogger.logAgentCreation(agentMock); // one commit
        Thread.sleep(10);

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
        SimulationLogger<SpatialAgent<?, ?, ?>> jdbcLogger = SimulationLoggers.createJDBCLogger(mock, commitThreshold);
        final SpatialAgent agentMock = mock(SpatialAgent.class);
        when(agentMock.getPopulation()).thenReturn(Population.named(""));
        when(agentMock.getTraits()).thenReturn(ImmutableFunctionalList.of());

        // when
        jdbcLogger.logAgentCreation(agentMock); // two commits
        jdbcLogger.logAgentCreation(agentMock); // one commit
        Thread.sleep(10);

        // then
        verify(connectionMock, times(1)).commit();
    }
}
