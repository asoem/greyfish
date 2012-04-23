package org.asoem.greyfish.core.io;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.asoem.greyfish.core.individual.Agent;
import org.asoem.greyfish.core.simulation.Simulation;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

/**
 * User: christoph
 * Date: 11.04.12
 * Time: 12:57
 */
public class JSONLogger implements SimulationLogger {

    private final JsonGenerator jsonGenerator;
    private final GZIPOutputStream outputStream;

    @Inject
    public JSONLogger(@Assisted Simulation simulation) {

        final JsonFactory jsonFactory = new JsonFactory();
        try {
            outputStream = new GZIPOutputStream(new FileOutputStream(simulation.getUUID() + ".json.gz"));
            jsonGenerator = jsonFactory.createJsonGenerator(outputStream, JsonEncoding.UTF8);
            //jsonGenerator.writeStartArray();
        } catch (IOException e) {
            closeStream();
            throw new IOError(e);
        }
    }

    @Override
    public void close() {
        try {
            //jsonGenerator.writeEndArray();
            jsonGenerator.close();
        } catch (IOException e) {
            throw new IOError(e);
        }
        finally {
            closeStream();
        }
    }

    @Override
    public void addAgent(Agent agent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private void closeStream() {
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public void addEvent(int eventId, UUID uuid, int currentStep, int agentId, String populationName, double[] coordinates, String source, String title, String message) {
        try {
            jsonGenerator.writeStartObject();

            jsonGenerator.writeNumberField("eventId", eventId);
            jsonGenerator.writeBinaryField("uuid", Bytes.concat(Longs.toByteArray(uuid.getMostSignificantBits()), Longs.toByteArray(uuid.getLeastSignificantBits())));
            jsonGenerator.writeNumberField("step", currentStep);
            jsonGenerator.writeNumberField("agentId", agentId);
            jsonGenerator.writeStringField("pop", populationName);
            jsonGenerator.writeArrayFieldStart("coordinates");
            for (double c : coordinates)
                jsonGenerator.writeNumber(c);
            jsonGenerator.writeEndArray();
            jsonGenerator.writeStringField("source", source);
            jsonGenerator.writeStringField("title", title);
            jsonGenerator.writeStringField("message", message);

            jsonGenerator.writeEndObject();

            jsonGenerator.writeRaw('\n');
        } catch (IOException e) {
            closeStream();
            throw new IOError(e);
        }
    }
}
