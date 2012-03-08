package org.asoem.greyfish.core.io.persistence;

import com.google.common.io.CharStreams;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.utils.EvaluatingMarkovChain;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.asoem.greyfish.utils.persistence.Persister;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.strategy.CycleStrategy;

import java.io.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:18
 */
public class SimpleXMLPersister implements Persister {

    private final Logger LOGGER = LoggerFactory.getLogger(SimpleXMLPersister.class);

    private final Registry registry = new Registry();
    private final Serializer serializer = new org.simpleframework.xml.core.Persister(
            new RegistryStrategy(registry, new CycleStrategy("id", "ref")),
            new FixedEnumMatcher());

    public SimpleXMLPersister() {
        try {
            registry.bind(GreyfishExpression.class, GreyfishExpressionConverter.class);
            registry.bind(EvaluatingMarkovChain.class, EvaluatingMarkovChainConverter.class);
        } catch (Exception e) {
            LOGGER.error("Binding converter to registry failed", e);
        }
    }

    @Override
    public <T> T deserialize(File file, Class<T> clazz) throws Exception {
        LOGGER.debug("Reading from: {}", file.getAbsolutePath());
        return deserialize(new BufferedReader(new FileReader(file)), clazz);
    }

    @Override
    public <T> T deserialize(Reader reader, Class<T> clazz) throws Exception {
        try {
            return serializer.read(clazz, reader);
        } catch (Exception e1) {
            LOGGER.error("Deserialization failed", e1);
            throw e1;
        }
        finally {
            reader.close();
        }
    }

    @Override
    public void serialize(Object object, File file) throws Exception {
        if (checkNotNull(file).exists())
            checkArgument(file.canWrite(), "Cannot overwrite file: " + file.getAbsolutePath());

        serialize(object, new BufferedWriter(new FileWriter(file)));
        LOGGER.debug("Object written to: {}", file.getAbsolutePath());
    }

    @Override
    public void serialize(Object object, Writer writer) throws Exception {
        checkNotNull(writer);
        checkNotNull(object);

        LOGGER.debug("Serializing object of type {}", object.getClass().getName());

        try {
            final StringWriter stringWriter = new StringWriter();
            serializer.write(object, stringWriter);
            LOGGER.debug("Serialization result:\n{}", stringWriter.toString());
            CharStreams.copy(new StringReader(stringWriter.toString()), writer);
        } catch (Exception e) {
            LOGGER.error("Serialization failed", e);
            throw new RuntimeException("Object not serializable", e);
        } finally {
            writer.close();
        }
    }
}
