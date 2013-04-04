package org.asoem.greyfish.utils.persistence;

import com.google.common.io.Closeables;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:37
 */
public final class Persisters {

    private Persisters() {}

    /**
     * Create a copy of the given object {@code o} by serializing it with the given {@code Persister}.
     * No guarantees can be made about how exact the copy will be, as this is dependent of the {@code Persister} implementation.
     * @param o the object you wish to copy
     * @param persister the {@code Persister} to use for the serialization process
     * @param <T> the type of the object
     * @return a copy of {@code o}
     * @throws Exception if some errors occur during the serialization process
     */
    public static <T> T createCopy(final T o, final Persister persister) throws Exception {
        checkNotNull(o);
        checkNotNull(persister);

        final PipedOutputStream pipedOutputStream = new PipedOutputStream();
        final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

        final Future<T> deserializeFuture = Executors.newSingleThreadExecutor().submit(new Callable<T>() {
            @SuppressWarnings("unchecked") // safe if persister is implemented correctly
            @Override
            public T call() throws Exception {
                try {
                    return (T) persister.deserialize(pipedInputStream, o.getClass());
                } catch (Exception e) {
                    pipedInputStream.close();
                    throw e;
                }
            }
        });

        try {
            try {
                persister.serialize(o, pipedOutputStream);
            } catch (Exception e) {
                if (!deserializeFuture.isDone()) {
                    deserializeFuture.cancel(true);
                    throw e;
                }
                else { // the future task had an exception and closed the stream, which caused this exception
                    deserializeFuture.get(); // throws the exception
                    throw new AssertionError("unreachable");
                }
            }
            finally {
                pipedOutputStream.close();
            }
            return deserializeFuture.get(3, TimeUnit.SECONDS);
        } finally {
            pipedInputStream.close();
        }
    }

    public static <T> T deserialize(Persister persister, InputSupplier<? extends InputStream> inputSupplier, Class<T> clazz) throws IOException, ClassCastException, ClassNotFoundException {
        final InputStream input = inputSupplier.getInput();
        boolean threw = true;
        try {
            final T object = persister.deserialize(input, clazz);
            threw = false;
            return object;
        } finally {
            Closeables.close(input, threw);
        }
    }

    public static void serialize(Persister persister, Object object, OutputSupplier<? extends OutputStream> outputSupplier) throws IOException {
        final OutputStream output = outputSupplier.getOutput();
        boolean threw = true;
        try {
            persister.serialize(object, output);
            threw = false;
        } finally {
            Closeables.close(output, threw);
        }
    }

    public static Persister javaSerialization() {
        return JavaPersister.INSTANCE;
    }
}
