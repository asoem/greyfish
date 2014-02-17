package org.asoem.greyfish.utils.persistence;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;

import java.io.*;

import static com.google.common.base.Preconditions.checkNotNull;


public enum JavaPersister implements Persister {
    INSTANCE;

    @Override
    public <T> T deserialize(final File file, final Class<T> clazz) throws IOException, ClassNotFoundException {
        return deserialize(Files.newInputStreamSupplier(file), clazz);
    }

    private <T> T deserialize(final InputSupplier<? extends InputStream> inputSupplier, final Class<T> clazz) throws IOException, ClassCastException, ClassNotFoundException {
        final InputStream input = inputSupplier.getInput();
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
    public <T> T deserialize(final InputStream inputStream, final Class<T> clazz) throws IOException, ClassCastException, ClassNotFoundException {
        checkNotNull(inputStream);
        checkNotNull(clazz);
        final ObjectInputStream in = new ObjectInputStream(inputStream);
        return clazz.cast(in.readObject());
    }

    private void serialize(final Object object, final OutputSupplier<? extends OutputStream> outputSupplier) throws IOException {
        final OutputStream output = outputSupplier.getOutput();
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
        serialize(object, Files.newOutputStreamSupplier(file));
    }

    @Override
    public void serialize(final Object object, final OutputStream outputStream) throws IOException {
        checkNotNull(object);
        checkNotNull(outputStream);
        final ObjectOutputStream out = new ObjectOutputStream(outputStream);
        out.writeObject(object);
    }
}
