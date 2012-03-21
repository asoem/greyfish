package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.simulation.Simulation;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * User: christoph
 * Date: 20.03.12
 * Time: 14:53
 */
public class CassandraLoggerTest {
    @Test
    public void testAddEvent() throws Exception {
        // given
        CassandraLogger cassandraLogger = new CassandraLogger(mock(Simulation.class));
        AgentEvent agentEvent = new AgentEvent("TestSimulation", 42, 1, "TestPopulation", "BerkeleyLoggerTest", "TestEvent", "Lorem ipsum", new double[] {2.5435, 0.54534});

        // when
        try {
            cassandraLogger.addEvent(agentEvent);
        }
        finally {
            cassandraLogger.close();
        }

        // then
        assert true;
    }
}
