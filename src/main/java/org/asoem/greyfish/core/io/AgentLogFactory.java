package org.asoem.greyfish.core.io;

import java.io.IOException;

/**
 * User: christoph
 * Date: 28.04.11
 * Time: 15:51
 */
public interface AgentLogFactory {

    void commit(AgentLog log) throws IOException;
    AgentLog newAgentLog();
}
