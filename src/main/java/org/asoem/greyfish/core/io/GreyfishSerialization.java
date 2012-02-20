package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.eval.GreyfishExpression;
import org.asoem.greyfish.utils.logging.Logger;
import org.asoem.greyfish.utils.logging.LoggerFactory;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Registry;
import org.simpleframework.xml.convert.RegistryStrategy;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.transform.Matcher;

import java.io.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public enum GreyfishSerialization {

    INSTANCE;

    private final Logger LOGGER = LoggerFactory.getLogger(GreyfishSerialization.class);

    private final Matcher MATCHER = new FixedEnumMatcher();
    private final Registry registry = new Registry();
    private final Serializer serializer = new Persister(new RegistryStrategy(registry, new CycleStrategy("id", "ref")), MATCHER);

    GreyfishSerialization() {
        try {
            registry.bind(GreyfishExpression.class, GreyfishExpressionConverter.class);
        } catch (Exception e) {
            LOGGER.error("Binding converter to registry failed", e);
        }
    }

    public static <T> T deserializeFile(File file, Class<T> clazz) throws Exception {
        INSTANCE.LOGGER.debug("Reading from: {}", file.getAbsolutePath());
        return deserializeFile(new FileInputStream(file), clazz);
    }

    public static <T> T deserializeFile(InputStream iStream, Class<T> clazz) throws Exception {
        try {
            return INSTANCE.serializer.read(clazz, iStream);
        } catch (Exception e1) {
            INSTANCE.LOGGER.error("Deserialization failed", e1);
            throw e1;
        }
    }

    public static <T> boolean validateFile(File file, Class<T> clazz) {
        try {
            return INSTANCE.serializer.validate(clazz, file);
        } catch (Exception e) {
            INSTANCE.LOGGER.error("Unable to deserialize file {} to class {}", file, clazz, e);
        }
        return false;
    }

    public static void serializeObject(File file, Object object) throws Exception {
        if (checkNotNull(file).exists())
            checkArgument(file.canWrite(), "Cannot overwrite file: " + file.getAbsolutePath());

        serializeObject(new FileOutputStream(file), object);
        INSTANCE.LOGGER.debug("Object written to: {}", file.getAbsolutePath());
    }

    public static void serializeObject(OutputStream oStream, Object object) throws RuntimeException {
        checkNotNull(oStream);
        checkNotNull(object);

        INSTANCE.LOGGER.debug("Serializing object of type {}", object.getClass().getName());

        try {
            final File tempFile = File.createTempFile("Greyfish", "xml");
            INSTANCE.serializer.write(object, tempFile);
            INSTANCE.serializer.write(object, oStream);
        } catch (Exception e) {
            INSTANCE.LOGGER.error("Serialization failed", e);
            throw new RuntimeException("Object not serializable", e);
        }
    }

}
