package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.individual.Prototype;
import org.asoem.greyfish.core.scenario.Scenario;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.transform.Matcher;
import org.simpleframework.xml.transform.Transform;

import java.awt.*;
import java.io.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.io.GreyfishLogger.CORE_LOGGER;

public class  GreyfishSerialization {

    private final static Matcher MATCHER = new Matcher() {

        @Override
        public Transform match(final Class arg0) throws Exception {
            if(arg0.isEnum()
                    || arg0.getSuperclass() != null
                    && arg0.getSuperclass().isEnum()) { // This is a Workaround for a java bug. See: http://forums.oracle.com/forums/thread.jspa?threadID=1035332
                return new Transform<Enum>() {

                    public Enum read(String value) throws Exception {
                        return Enum.valueOf(arg0, value);
                    }

                    public String write(Enum value) throws Exception {
                        return value.name();
                    }
                };
            }
            else if (Color.class.equals(arg0)) {
                return new Transform<Color>() {

                    @Override
                    public Color read(String arg0) throws Exception {
                        return new Color(Integer.valueOf(arg0));
                    }

                    @Override
                    public String write(Color arg0) throws Exception {
                        return String.valueOf(arg0.getRGB());
                    }

                };
            }
            return null;
        }
    };

    private final static Serializer serializer = new Persister(new CycleStrategy("id", "ref"), MATCHER);

    public static void writeScenario(Scenario scenario, File dest) throws Exception {
        serializeObject(dest, scenario);
    }

    public static Prototype readPrototype(File source) throws Exception {
        return deserializeFile(source, Prototype.class);
    }

    public static Scenario readScenario(File source) throws Exception {
        return deserializeFile(source, Scenario.class);
    }

    public static void writeObject(File dest, Object object) throws Exception {
        serializeObject(dest, object);
    }

    public static <T> T deserializeFile(File file, Class<T> clazz) throws Exception {
        if (CORE_LOGGER.hasDebugEnabled()) CORE_LOGGER.debug("Reading from: " + file.getAbsolutePath());
        return deserializeFile(new FileInputStream(file), clazz);
    }

    public static <T> T deserializeFile(InputStream iStream, Class<T> clazz) throws Exception {
        try {
            return serializer.read(clazz, iStream);
        } catch (Exception e1) {
            CORE_LOGGER.error("Deserialization failed", e1);
            throw e1;
        }
    }

    public static <T> boolean validateFile(File file, Class<T> clazz) {
        try {
            return serializer.validate(clazz, file);
        } catch (Exception e) {
            if (CORE_LOGGER.hasDebugEnabled()) CORE_LOGGER.debug("Unable to deserialize file to " + clazz + ": " + file, e);
        }
        return false;
    }

    public static void serializeObject(File file, Object object) throws Exception {
        if (checkNotNull(file).exists())
            checkArgument(file.canWrite(), "Cannot overwrite file: " + file.getAbsolutePath());

        serializeObject(new FileOutputStream(file), object);
        if (CORE_LOGGER.hasDebugEnabled()) CORE_LOGGER.debug("Object written to: " + file.getAbsolutePath());
    }

    public static void serializeObject(OutputStream oStream, Object object) throws RuntimeException {
        checkNotNull(oStream);
        checkNotNull(object);

        if (CORE_LOGGER.hasDebugEnabled()) CORE_LOGGER.debug("Serializing object of type " + object.getClass().getName());

        try {
            final File tempFile = File.createTempFile("Greyfish", "xml");
            serializer.write(object, tempFile);
            serializer.write(object, oStream);
        } catch (Exception e) {
            CORE_LOGGER.error("Serialization failed", e);
            throw new RuntimeException("Object not serializable", e);
        }
    }

}
