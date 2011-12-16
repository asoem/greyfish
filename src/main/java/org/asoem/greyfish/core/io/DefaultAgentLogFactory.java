package org.asoem.greyfish.core.io;

import com.google.common.base.Joiner;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * User: christoph
 * Date: 28.04.11
 * Time: 16:13
 */
public class DefaultAgentLogFactory implements AgentLogFactory {

    private boolean headingPrinted = false;
    private final String fileName;

    public DefaultAgentLogFactory(String fileName) {
        this.fileName = fileName;
    }

    public void commit(AgentLog log) throws IOException {
        Writer writer = new FileWriter(fileName, true);

        if (!headingPrinted) {
            // print heading
            writer.append(Joiner.on('\t').join(log.getMap().keySet())).append('\n');
            headingPrinted = true;
        }

        writer.append(Joiner.on('\t').join(log.getMap().values())).append('\n');

        writer.close();
    }

    @Override
    public AgentLog newAgentLog() {
        return new AgentLog(this);
    }
}
