package org.asoem.greyfish.core.io;

import com.google.inject.Inject;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 13:27
 */
public class AgentEventLoggerFactory {
    @Inject
    private static AgentEventLogger logger;

    public static AgentEventLogger getLogger() {
        return logger;
    }
}
