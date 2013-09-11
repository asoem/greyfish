package org.asoem.greyfish.utils.persistence;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:11
 */
public interface Persister {
    public <T> T deserialize(File file, Class<T> clazz) throws IOException, ClassCastException, ClassNotFoundException;
    public <T> T deserialize(InputStream inputStream, Class<T> clazz) throws IOException, ClassCastException, ClassNotFoundException;

    public void serialize(Object object, File file) throws IOException;
    public void serialize(Object object, OutputStream outputStream) throws IOException;
}