package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.simulation.Simulation;
import org.junit.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;

/**
 * User: christoph
 * Date: 09.03.12
 * Time: 16:46
 */
public class BerkeleyLoggerTest {

    @Test
    public void testAddEvent() throws Exception {
        // given
        BerkeleyLogger berkeleyLogger = new BerkeleyLogger(mock(Simulation.class));
        AgentEvent agentEvent = new AgentEvent(0, UUID.randomUUID(), 42, 1, "TestPopulation", new double[] {2.5435, 0.54534}, "BerkeleyLoggerTest", "TestEvent", "Lorem ipsum");

        // when
        berkeleyLogger.addEvent(agentEvent);

        // then
        for (AgentEvent event : berkeleyLogger.getLoggedEvents())
            System.out.println(event);

        berkeleyLogger.close();
        assert true;
    }
}
