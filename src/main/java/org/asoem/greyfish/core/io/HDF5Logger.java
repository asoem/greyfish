package org.asoem.greyfish.core.io;

import ch.systemsx.cisd.hdf5.HDF5CompoundType;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5CompoundWriter;
import ch.systemsx.cisd.hdf5.IHDF5Writer;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.asoem.greyfish.core.simulation.Simulation;

import java.io.Flushable;
import java.io.IOError;
import java.io.IOException;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 02.04.12
 * Time: 10:53
 */
public class HDF5Logger implements SimulationLogger {

    private final IHDF5Writer writer;
    private final BufferedCompoundArrayWriter<AgentEvent> bufferedWriter;

    @Inject
    private HDF5Logger(@Assisted Simulation simulation) {
        writer = HDF5Factory.open(simulation.getUUID() + ".h5");

        final HDF5CompoundType<AgentEvent> inferredType = writer.compounds().getInferredType(AgentEvent.class);
        writer.compounds().createArray("agent_events", inferredType, 10000);

        bufferedWriter = new BufferedCompoundArrayWriter<AgentEvent>(
                "agent_events", writer.compounds(), inferredType,
                Lists.<AgentEvent>newArrayListWithCapacity(100), 100);
    }

    @Override
    public void addEvent(AgentEvent event) {
        try {
            bufferedWriter.add(event);
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public void close() {
        try {
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new IOError(e);
        }
        writer.close();
    }

    public static class BufferedCompoundArrayWriter<T> implements Flushable {
        private final Collection<T> storage;
        private final String path;
        private final IHDF5CompoundWriter compoundWriter;
        private final HDF5CompoundType<T> compoundType;
        private final int bufferLimit;
        private int offset = 0;

        private BufferedCompoundArrayWriter(String path, IHDF5CompoundWriter compoundWriter, HDF5CompoundType<T> compoundType, Collection<T> buffer, int bufferLimit) {
            this.path = path;
            this.compoundWriter = checkNotNull(compoundWriter);
            this.compoundType = checkNotNull(compoundType);
            checkArgument(bufferLimit > 0, "The bufferLimit must be > 0");
            this.bufferLimit = bufferLimit;
            this.storage = buffer;
        }

        public void add(T element) throws IOException {
            storage.add(element);
            if (storage.size() == bufferLimit) {
                flush();
            }
        }

        @Override
        public void flush() throws IOException {
            compoundWriter.writeArrayBlockWithOffset(path, compoundType, (T[]) storage.toArray(), offset);
            offset += storage.size();
            storage.clear();
        }
    }
}
