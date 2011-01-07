package org.asoem.greyfish.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.asoem.greyfish.core.individual.Individual;
import org.asoem.greyfish.core.scenario.Scenario;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.strategy.Strategy;

import com.google.common.base.Preconditions;

public class GreyfishSerialization {

	private final static Strategy strategy = new CycleStrategy("id", "ref");
	private final static Serializer serializer = new Persister(strategy, GreyfishMatcher.getInstance());

	public static void writeScenario(Scenario scenario, File dest) throws Exception {
		serializeOject(dest, scenario);
	}

	public static Individual readPrototype(File source) throws Exception {
		return deserializeFile(source, Individual.class);
	}

	public static Scenario readScenario(File source) throws Exception {
		return deserializeFile(source, Scenario.class);
	}

	public static void writeObject(File dest, Object object) throws Exception {
		serializeOject(dest, object);
	}

	public static <T> T deserializeFile(File file, Class<T> clazz) throws Exception {
		if (GreyfishLogger.isDebugEnabled())
			GreyfishLogger.debug("Reading from: " + file.getAbsolutePath());
		return deserializeFile(new FileInputStream(file), clazz);
	}
	
	public static <T> T deserializeFile(InputStream iStream, Class<T> clazz) throws Exception {
		try {
			return serializer.read(clazz, iStream);
		} catch (Exception e1) {
			GreyfishLogger.error("Deserialization failed", e1);
			throw e1;
		}
	}
	
	public static <T> boolean validateFile(File file, Class<T> clazz) {
		try {
			return serializer.validate(clazz, file);
		} catch (Exception e) {
			if (GreyfishLogger.isDebugEnabled())
				GreyfishLogger.debug("XML error", e);
		}
		return false;
	}

	public static <T> void serializeOject(File file, Object object) throws Exception {
		Preconditions.checkNotNull(file);
		if (file.exists())
			Preconditions.checkArgument( file.canWrite(), "Cannot overwrite file: " + file.getAbsolutePath());

		serializeOject(new FileOutputStream(file), object);
		if (GreyfishLogger.isDebugEnabled())
			GreyfishLogger.debug("Object written to: " + file.getAbsolutePath());
	}

	public static <T> void serializeOject(OutputStream oStream, Object object) throws Exception {
		Preconditions.checkNotNull(oStream);
		Preconditions.checkNotNull(object);

		try {
			final File tempFile = File.createTempFile("Greyfish", "xml");
			serializer.write(object, tempFile);
		} catch (Exception e) {
			GreyfishLogger.error("Serialization failed", e);
			throw new RuntimeException("Object not serializable", e);
		}

		try {
			serializer.write(object, oStream);
		} catch (Exception e) {
			GreyfishLogger.error("Serialization failed", e);
			throw e;
		}
	}
}
