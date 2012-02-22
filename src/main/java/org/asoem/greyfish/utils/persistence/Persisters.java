package org.asoem.greyfish.utils.persistence;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:37
 */
public class Persisters {
    /**
     * Create a copy of the given object {@code o} by serializing it with the given {@code Persister}.
     * No guarantees can be made about how exact the copy will be, as this is dependent of the {@code Persister} implementation.
     * @param o the object you wish to copy
     * @param clazz the clazz of the object
     * @param persister the {@code Persister} to use for the serialization process
     * @param <T> the type of the object
     * @return a copy of {@code o}
     * @throws Exception if some errors occur during the serialization process
     */
    public static <T> T createCopy(final Object o, Class<T> clazz, final Persister persister) throws Exception {
        checkNotNull(o);
        checkNotNull(clazz);
        checkNotNull(persister);

        final PipedOutputStream pipedOutputStream = new PipedOutputStream();
        final PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
        final OutputStreamWriter writer = new OutputStreamWriter(pipedOutputStream);

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    persister.serialize(o, writer);
                } catch (Exception e) {
                    Logger.getLogger(Persisters.class.getCanonicalName()).log(Level.SEVERE,
                            "Error in serialization thread (persister=" + persister + "; object=" + o + "; writer=" + writer + ")", e);
                }
            }
        });

        return persister.deserialize(new InputStreamReader(pipedInputStream), clazz);
    }
}
