package org.asoem.greyfish.core.io;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.Service;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.asoem.greyfish.core.individual.Agent;

/**
 * User: christoph
 * Date: 23.03.12
 * Time: 12:34
 */
public class LoadLogger implements SimulationLogger {

    private final DescriptiveStatistics statistics = new DescriptiveStatistics();

    private int logCount = 0;

    private final Service service = new AbstractExecutionThreadService() {

        @Override
        protected void run() throws Exception {
            // every second do
            while (isRunning()) {
                Thread.sleep(1000);
                statistics.addValue(logCount);

                if (logCount > 1000) {
                    logCount = 0;
                }

                logCount = 0;
            }
        }
    };

    public LoadLogger() {
        service.start();
    }

    @Override
    public void logAgentCreation(Agent agent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void logAgentEvent(int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
        ++logCount;
    }
}
