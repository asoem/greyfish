package org.asoem.greyfish.core.io;

import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.Service;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.asoem.greyfish.core.individual.Agent;

import java.util.UUID;

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
    public void close() {
        service.stopAndWait();
        System.out.println(statistics.toString());
        System.out.println(Doubles.join(" ", statistics.getValues()));
    }

    @Override
    public void addAgent(Agent agent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addEvent(int eventId, UUID uuid, int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
        ++logCount;
    }
}
