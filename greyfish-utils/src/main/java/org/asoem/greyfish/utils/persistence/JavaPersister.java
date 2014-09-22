package org.asoem.greyfish.utils.persistence;

import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import com.google.common.io.Files;

import java.io.*;

import static com.google.common.base.Preconditions.checkNotNull;


public enum JavaPersister implements Persister {
    INSTANCE;

    @Override
    public <T> T deserialize(final File file, final Class<T> clazz) throws IOException, ClassNotFoundException {
        return deserialize(Files.asByteSource(file), clazz);
    }

    private <T> T deserialize(final ByteSource byteSource, final Class<T> clazz) throws IOException, ClassNotFoundException {
        final InputStream input = byteSource.openBufferedStream();
        boolean threw = true;
        try {
            final T object = deserialize(input, clazz);
            threw = false;
            return object;
        } finally {
            Closeables.close(input, threw);
        }
    }

    @Override
    public <T> T deserialize(final InputStream inputStream, final Class<T> clazz) throws IOException, ClassNotFoundException {
        checkNotNull(inputStream);
        checkNotNull(clazz);
        final ObjectInputStream in = new ObjectInputStream(inputStream);
        return clazz.cast(in.readObject());
    }

    private void serialize(final Object object, final ByteSink byteSink) throws IOException {
        final OutputStream output = byteSink.openBufferedStream();
        boolean threw = true;
        try {
            serialize(object, output);
            threw = false;
        } finally {
            Closeables.close(output, threw);
        }
    }

    @Override
    public void serialize(final Object object, final File file) throws IOException {
        serialize(object, Files.asByteSink(file));
    }

    @Override
    public void serialize(final Object object, final OutputStream outputStream) throws IOException {
        checkNotNull(object);
        checkNotNull(outputStream);
        final ObjectOutputStream out = new ObjectOutputStream(outputStream);
        out.writeObject(object);
    }
}
