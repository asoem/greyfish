package org.asoem.greyfish.core.io;

import org.junit.Test;

/**
 * User: christoph
 * Date: 09.03.12
 * Time: 16:46
 */
public class BerkeleyLoggerTest {
    @Test
    public void testAddEvent() throws Exception {
        // given
        BerkeleyLogger berkeleyLogger = new BerkeleyLogger();
        AgentEvent agentEvent = new AgentEvent("TestSimulation", 42, 1, "TestPopulation", "BerkeleyLoggerTest", "TestEvent", "Lorem ipsum", new double[] {2.5435, 0.54534});

        // when
        berkeleyLogger.addEvent(agentEvent);

        // then
        for (AgentEvent event : berkeleyLogger.getLoggedEvents())
            System.out.println(event);

        berkeleyLogger.close();
        assert true;
    }
}
