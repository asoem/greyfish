/*
 * Copyright (C) 2015 The greyfish authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.asoem.greyfish.utils.persistence;

import com.google.common.io.Closeables;
import com.google.common.io.Closer;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;


public final class Persisters {

    private Persisters() {}

    /**
     * Create a copy of the given object {@code o} by serializing it with the given {@code Persister}. What copy means
     * in this context, dependents fully on the {@code Persister} implementation.
     *
     * @param o         the object you wish to copy
     * @param persister the {@code Persister} to use for the serialization process
     * @return a copy of {@code o}
     * @throws Exception if some errors occur during the serialization process
     */
    public static <T> T copyAsync(final T o, final Persister persister) throws Exception {
        checkNotNull(o);
        checkNotNull(persister);

        try (Closer closer = Closer.create()) {
            final PipedOutputStream pipedOutputStream = closer.register(new PipedOutputStream());
            final PipedInputStream pipedInputStream = closer.register(new PipedInputStream(pipedOutputStream));

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
                persister.serialize(o, pipedOutputStream);
            } catch (Exception e) {
                if (!deserializeFuture.isDone()) {
                    deserializeFuture.cancel(true);
                    throw e;
                } else { // the future task had an exception and closed the stream, which caused this exception
                    deserializeFuture.get(); // throws the exception
                    throw new AssertionError("unreachable");
                }
            }
            return deserializeFuture.get(3, TimeUnit.SECONDS);
        }
    }

    /**
     * Create a copy of the given object {@code o} through serialization unsing the given {@code persister}.
     *
     * @param o         the object to copy
     * @param persister the persister to use for serialization and deserialization.
     * @param clazz     the class to cast the deserialized object to
     * @param <T>       the type of {@code o}
     * @return a copy of {@code o}, as implemented by the given {@code persister}
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static <T> T copy(final T o, final Persister persister, final Class<T> clazz)
            throws IOException, ClassNotFoundException {

        try (Closer closer = Closer.create()) {
            final ByteArrayOutputStream outputStream = closer.register(new ByteArrayOutputStream());
            persister.serialize(o, outputStream);
            final ByteArrayInputStream inputStream = closer.register(
                    new ByteArrayInputStream(outputStream.toByteArray()));
            return persister.deserialize(inputStream, clazz);
        }
    }

    public static <T> T deserialize(final Persister persister,
                                    final InputSupplier<? extends InputStream> inputSupplier,
                                    final Class<T> clazz) throws IOException, ClassNotFoundException {
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

    public static void serialize(final Persister persister, final Object object,
                                 final OutputSupplier<? extends OutputStream> outputSupplier) throws IOException {
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
