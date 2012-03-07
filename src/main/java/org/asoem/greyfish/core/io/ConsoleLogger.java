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
                        event.getStep() + "\t" +
                        event.getPopulationName() + "\t" +
                        event.getAgentId() + "\t" +
                        event.getSource() + "\t" +
                        event.getLocatable2D() + "\t" +
                        event.getKey() + "\t" +
                        event.getValue() + "\n");
    }
}
