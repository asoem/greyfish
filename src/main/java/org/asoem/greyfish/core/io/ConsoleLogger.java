package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.agent.Agent;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 13:47
 */
public class ConsoleLogger<A extends Agent<A, ?>> implements SimulationLogger<A> {

    @Override
    public void logAgentCreation(A agent) {
    }

    @Override
    public void logAgentEvent(A agent, int currentStep, String source, String title, String message) {
        System.out.println(
                currentStep + "\t" +
                        agent.getPopulation() + "\t" +
                        agent.getId() + "\t" +
                        source + "\t" +
                        //Doubles.join(",", agent.getProjection()) + "\t" +
                        title + "\t" +
                        message + "\n");
    }
}
