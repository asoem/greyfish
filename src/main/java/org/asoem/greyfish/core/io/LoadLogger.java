package org.asoem.greyfish.core.io;

import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.Service;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * User: christoph
 * Date: 23.03.12
 * Time: 12:34
 */
public class LoadLogger implements SimulationLogger {

    DescriptiveStatistics statistics = new DescriptiveStatistics();

    int logCount = 0;

    private final Service service = new AbstractExecutionThreadService() {

        @Override
        protected void run() throws Exception {
            // every second do
            while (isRunning()) {
                Thread.sleep(1000);
                statistics.addValue(logCount);
                logCount = 0;
            }
        }
    };

    public LoadLogger() {
        service.start();
    }

    @Override
    public void addEvent(AgentEvent event) {
        logCount++;
    }

    @Override
    public void close() {
        service.stopAndWait();
        System.out.println(statistics.toString());
        System.out.println(Doubles.join(" ", statistics.getValues()));
    }
}
