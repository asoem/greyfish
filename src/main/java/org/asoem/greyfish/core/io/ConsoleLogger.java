package org.asoem.greyfish.core.io;

import com.google.common.primitives.Doubles;
import org.asoem.greyfish.core.individual.Agent;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 13:47
 */
public class ConsoleLogger implements SimulationLogger {

    @Override
    public void logAgentCreation(Agent agent) {
    }

    @Override
    public void logAgentEvent(int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
        System.out.println(
                currentStep + "\t" +
                        populationName + "\t" +
                        agentId + "\t" +
                        source + "\t" +
                        Doubles.join(",", coordinates) + "\t" +
                        title + "\t" +
                        message + "\n");
    }
}
