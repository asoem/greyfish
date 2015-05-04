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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class BufferedJDBCLoggerTest {
    @Test
    public void testCommitThreshold1() throws Exception {
        // given
        final int commitThreshold = 1;

        // then
        testCommitInvocations(commitThreshold);
    }

    @Test
    public void testCommitThreshold3() throws Exception {
        // given
        final int commitThreshold = 3;

        // then
        testCommitInvocations(commitThreshold);
    }

    void testCommitInvocations(final int commitThreshold) throws SQLException, InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
        // given
        final int expectedCommits = (int) Math.round(3.0 / commitThreshold);
        final AtomicInteger commitInvocations = new AtomicInteger(0);
        final Connection connectionMock = mock(Connection.class);
        given(connectionMock.prepareStatement(any(String.class))).willReturn(mock(PreparedStatement.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocationOnMock) throws Throwable {
                commitInvocations.incrementAndGet();
                return null;
            }
        }).when(connectionMock).commit();
        final ConnectionManager mock = mock(ConnectionManager.class);
        when(mock.get()).thenReturn(connectionMock);
        final BufferedJDBCLogger jdbcLogger = new BufferedJDBCLogger(mock, commitThreshold);

        // when
        jdbcLogger.logAgentCreation(0, "", 0, "", ImmutableSet.<Integer>of(), ImmutableMap.<String, Object>of()); // two commits
        jdbcLogger.logAgentCreation(0, "", 0, "", ImmutableSet.<Integer>of(), ImmutableMap.<String, Object>of()); // one commit
        MoreExecutors.newDirectExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new AssertionError(e);
                    }
                } while (commitInvocations.get() < expectedCommits);
            }
        }).get(1, TimeUnit.SECONDS);

        // then
        //   success!
    }

    private interface TestEnvironment extends SpatialEnvironment2D<TestAgent, Space2D<TestAgent, Object2D>> {
    }

    private interface TestAgent extends SpatialAgent<TestAgent, TestContext, Object2D, AgentContext<TestAgent>> {
    }

    private interface TestContext extends BasicContext<TestEnvironment, TestAgent> {
    }
}
