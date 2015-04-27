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
