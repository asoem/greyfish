package org.asoem.greyfish.core.io.persistence;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.OutputSupplier;
import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.core.utils.EvaluatingMarkovChain;
import org.asoem.greyfish.utils.logging.SLF4JLogger;
import org.asoem.greyfish.utils.logging.SLF4JLoggerFactory;
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

    private final SLF4JLogger LOGGER = SLF4JLoggerFactory.getLogger(SimpleXMLPersister.class);

    private final Serializer serializer;

    public SimpleXMLPersister() {
        Registry registry = new Registry();
        serializer = new org.simpleframework.xml.core.Persister(
                new RegistryStrategy(registry, new CycleStrategy("id", "ref")),
                new FixedEnumMatcher());
        try {
            registry.bind(GreyfishExpression.class, GreyfishExpressionConverter.class)
                    .bind(EvaluatingMarkovChain.class, EvaluatingMarkovChainConverter.class);
        } catch (Exception e) {
            LOGGER.error("Binding converter to registry failed", e);
        }
    }

    public SimpleXMLPersister(Serializer serializer) {
        this.serializer = checkNotNull(serializer);
    }

    @Override
    public <T> T deserialize(File file, Class<T> clazz) throws IOException, ClassNotFoundException {
        LOGGER.debug("Reading from: {}", file.getAbsolutePath());
        return deserialize(new FileInputStream(file), clazz);
    }

    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> clazz) throws IOException {
        try {
            return serializer.read(clazz, new BufferedReader(new InputStreamReader(inputStream, "UTF-8")));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void serialize(Object object, OutputSupplier<? extends OutputStream> outputSupplier) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void serialize(Object object, File file) throws IOException {
        if (checkNotNull(file).exists())
            checkArgument(file.canWrite(), "Cannot overwrite file: " + file.getAbsolutePath());

        serialize(object, Files.newOutputStreamSupplier(file));
        LOGGER.debug("Object written to: {}", file.getAbsolutePath());
    }

    @Override
    public void serialize(Object object, OutputStream outputStream) throws IOException {
        checkNotNull(outputStream);
        checkNotNull(object);

        LOGGER.debug("Serializing object of type {}", object.getClass().getName());

        final StringWriter stringWriter = new StringWriter();
        try {
            serializer.write(object, stringWriter);
        } catch (Exception e) {
            throw new IOException(e);
        }
        LOGGER.debug("Serialization result:\n{}", stringWriter.toString());

        CharStreams.copy(new StringReader(stringWriter.toString()), new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8")));
    }
}
