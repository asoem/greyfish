package org.asoem.greyfish.core.io;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.MoreExecutors;
import org.asoem.greyfish.core.actions.AgentContext;
import org.asoem.greyfish.core.agent.BasicContext;
import org.asoem.greyfish.core.agent.SpatialAgent;
import org.asoem.greyfish.core.environment.SpatialEnvironment2D;
import org.asoem.greyfish.core.space.Space2D;
import org.asoem.greyfish.utils.space.Object2D;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.TimeUnit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class JDBCLoggerTest {
    @Test
    public void testCommitThreshold1() throws Exception {
        // given
        final int commitThreshold = 1;
        final Connection connectionMock = mock(Connection.class);
        given(connectionMock.prepareStatement(any(String.class))).willReturn(mock(PreparedStatement.class));
        final ConnectionManager mock = when(mock(ConnectionManager.class).get()).thenReturn(connectionMock).getMock();
        final BufferedJDBCLogger jdbcLogger = new BufferedJDBCLogger(mock, commitThreshold);

        // when
        jdbcLogger.logAgentCreation(0, "", 0, "", ImmutableSet.<Integer>of(), ImmutableMap.<String, Object>of()); // two commits
        jdbcLogger.logAgentCreation(0, "", 0, "", ImmutableSet.<Integer>of(), ImmutableMap.<String, Object>of()); // one commit
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
        SimulationLogger jdbcLogger = SimulationLoggers.createJDBCLogger(mock, commitThreshold);

        // when
        jdbcLogger.logAgentCreation(0, "", 0, "", ImmutableSet.<Integer>of(), ImmutableMap.<String, Object>of()); // two commits
        jdbcLogger.logAgentCreation(0, "", 0, "", ImmutableSet.<Integer>of(), ImmutableMap.<String, Object>of()); // one commit
        Thread.sleep(10);

        // then
        verify(connectionMock, times(1)).commit();
    }

    private interface TestEnvironment extends SpatialEnvironment2D<TestAgent, Space2D<TestAgent, Object2D>> {
    }

    private interface TestAgent extends SpatialAgent<TestAgent, TestContext, Object2D, AgentContext<TestAgent>> {
    }

    private interface TestContext extends BasicContext<TestEnvironment, TestAgent> {
    }
}
