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
