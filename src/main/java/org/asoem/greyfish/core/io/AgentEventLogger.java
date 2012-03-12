package org.asoem.greyfish.core.io;

/**
 * User: christoph
 * Date: 20.02.12
 * Time: 12:53
 */
public interface AgentEventLogger {
    void addEvent(AgentEvent event);
    void close();
}
