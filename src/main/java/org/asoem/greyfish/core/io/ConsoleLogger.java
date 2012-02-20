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
                event.getSimulation().getName() + "\t" +
                        event.getAgent().getId() + "\t" +
                        event.getStep() + "\t" +
                        event.getSource() + "\t" +
                        event.getCoordinates2D() + "\t" +
                        event.getKey() + "\t" +
                        event.getValue() + "\n");
    }
}
