package org.asoem.greyfish.core.io;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 13:47
 */
public class ConsoleLogger implements AgentEventLogger {
    @Override
    public void addEvent(AgentEvent event) {
        System.out.println(
                event.getSimulationId() + "\t" +
                        event.getSimulationStep() + "\t" +
                        event.getAgentPopulationName() + "\t" +
                        event.getAgentId() + "\t" +
                        event.getSourceOfEvent() + "\t" +
                        event.getLocatable2D() + "\t" +
                        event.getEventTitle() + "\t" +
                        event.getEventMessage() + "\n");
    }

    @Override
    public void close() {
        /* NOP */
    }
}
