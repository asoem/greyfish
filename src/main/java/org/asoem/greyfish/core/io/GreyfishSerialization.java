package org.asoem.greyfish.core.io;

import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.scenario.Scenario;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.strategy.Strategy;

import java.io.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.asoem.greyfish.core.io.GreyfishLogger.*;

public class  GreyfishSerialization {

	private final static Strategy strategy = new CycleStrategy("id", "ref");
	private final static Serializer serializer = new Persister(strategy, GreyfishMatcher.getInstance());

	public static void writeScenario(Scenario scenario, File dest) throws Exception {
		serializeObject(dest, scenario);
	}

	public static Individual readPrototype(File source) throws Exception {
		return deserializeFile(source, Individual.class);
	}

	public static Scenario readScenario(File source) throws Exception {
		return deserializeFile(source, Scenario.class);
	}

	public static void writeObject(File dest, Object object) throws Exception {
		serializeObject(dest, object);
	}

	public static <T> T deserializeFile(File file, Class<T> clazz) throws Exception {
		if (isDebugEnabled()) debug("Reading from: " + file.getAbsolutePath());
		return deserializeFile(new FileInputStream(file), clazz);
	}
	
	public static <T> T deserializeFile(InputStream iStream, Class<T> clazz) throws Exception {
		try {
			return serializer.read(clazz, iStream);
		} catch (Exception e1) {
			error("Deserialization failed", e1);
			throw e1;
		}
	}
	
	public static <T> boolean validateFile(File file, Class<T> clazz) {
		try {
			return serializer.validate(clazz, file);
		} catch (Exception e) {
			if (isDebugEnabled()) debug("Unable to deserialize file to " + clazz + ": " + file, e);
		}
		return false;
	}

	public static void serializeObject(File file, Object object) throws Exception {
		if (checkNotNull(file).exists())
			checkArgument(file.canWrite(), "Cannot overwrite file: " + file.getAbsolutePath());

		serializeObject(new FileOutputStream(file), object);
		if (isDebugEnabled()) debug("Object written to: " + file.getAbsolutePath());
	}

	public static void serializeObject(OutputStream oStream, Object object) throws Exception {
		checkNotNull(oStream);
		checkNotNull(object);

        if (isDebugEnabled()) debug("Serializing object of type " + object.getClass().getName());

		try {
			final File tempFile = File.createTempFile("Greyfish", "xml");
			serializer.write(object, tempFile);
			serializer.write(object, oStream);
		} catch (Exception e) {
			error("Serialization failed", e);
			throw new RuntimeException("Object not serializable", e);
		}
	}
}
