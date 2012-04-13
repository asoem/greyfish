package org.asoem.greyfish.core.io;

import ch.systemsx.cisd.hdf5.*;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.asoem.greyfish.core.simulation.Simulation;

import java.io.Flushable;
import java.io.IOError;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 02.04.12
 * Time: 10:53
 */
public class HDF5Logger implements SimulationLogger {

    private final IHDF5Writer writer;
    private final BufferedCompoundArrayWriter<HDF5AgentEvent> bufferedWriter;
    //private final HDF5EnumerationType enumerationType;

    @Inject
    private HDF5Logger(@Assisted Simulation simulation) {

        final String filePath = simulation.getUUID() + ".h5";

        writer = HDF5Factory.open(filePath);

        /*
        enumerationType = writer.enums().getType("population", Iterables.toArray(Iterables.transform(
                simulation.getPrototypes(), new Function<Agent, String>() {
            @Override
            public String apply(Agent agent) {
                return agent.getPopulation().getName();
            }
        }), String.class));

        final HDF5CompoundType<HDF5AgentEvent> inferredType = writer.compounds().getType(
                null,
                HDF5AgentEvent.class,
                HDF5CompoundMemberMapping.inferMapping(HDF5AgentEvent.class,
                        ImmutableMap.<String, HDF5EnumerationType>of("population", enumerationType)));
        */
        HDF5CompoundType<HDF5AgentEvent> inferredType = writer.compounds().getInferredType(HDF5AgentEvent.class);
        writer.compounds().createArray("agent_events", inferredType, 10000, HDF5GenericStorageFeatures.GENERIC_DEFLATE_KEEP);

        bufferedWriter = new BufferedCompoundArrayWriter<HDF5AgentEvent>(
                "agent_events", writer.compounds(), inferredType,
                Lists.<HDF5AgentEvent>newArrayListWithCapacity(100), 100);


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

    @Override
    public void addEvent(int eventId, UUID uuid, int currentStep, int agentId, String population, double[] coordinates, String source, String title, String message) {
        try {
            bufferedWriter.add(new HDF5AgentEvent(eventId, agentId, System.currentTimeMillis(), currentStep, source, population, coordinates, title, message) );
        } catch (IOException e) {
            throw new IOError(e);
        }
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

        @SuppressWarnings("unchecked")
        @Override
        public void flush() throws IOException {
            compoundWriter.writeArrayBlockWithOffset(path, compoundType, (T[]) storage.toArray(), offset);
            offset += storage.size();
            storage.clear();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private static class HDF5AgentEvent {

        private int eventId;

        private int agentId;

        @CompoundElement(typeVariant = HDF5DataTypeVariant.TIMESTAMP_MILLISECONDS_SINCE_START_OF_THE_EPOCH)
        private long createdAt;

        private int simulationStep;

        @CompoundElement(dimensions = 20)
        private String sourceOfEvent;

        @CompoundElement(dimensions = 20)
        private String population;

        private double locationX;

        private double locationY;

        @CompoundElement(dimensions = 20)
        private String eventTitle;

        @CompoundElement(dimensions = 20)
        private String eventMessage;

        private HDF5AgentEvent(int eventId, int agentId, long createdAt, int simulationStep, String sourceOfEvent, String population, double[] locationInSpace, String eventTitle, String eventMessage) {
            this.eventId = eventId;
            this.agentId = agentId;
            this.createdAt = createdAt;
            this.simulationStep = simulationStep;
            this.sourceOfEvent = sourceOfEvent;
            this.population = population;
            assert locationInSpace.length == 2;
            this.locationX = locationInSpace[0];
            this.locationY = locationInSpace[1];
            this.eventTitle = eventTitle;
            this.eventMessage = eventMessage;
        }
    }
}
