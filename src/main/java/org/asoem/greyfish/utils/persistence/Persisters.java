package org.asoem.greyfish.utils.persistence;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
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

        final Future<T> future = Executors.newSingleThreadExecutor().submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return (T) persister.deserialize(pipedInputStream, o.getClass());
            }
        });

        try {
            persister.serialize(o, pipedOutputStream);
            return future.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            future.cancel(true);
            throw (e);
        }
        finally {
            pipedInputStream.close();
            pipedOutputStream.close();
        }
    }
}
