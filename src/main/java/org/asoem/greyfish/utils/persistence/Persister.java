package org.asoem.greyfish.utils.persistence;

import org.asoem.greyfish.core.io.persistence.PersistenceException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:11
 */
public interface Persister {
    public <T> T deserialize(File file, Class<T> clazz) throws PersistenceException, FileNotFoundException;
    public <T> T deserialize(InputStream inputStream, Class<T> clazz) throws PersistenceException;
    public void serialize(Object object, File file) throws PersistenceException, FileNotFoundException;
    public void serialize(Object object, OutputStream outputStream) throws PersistenceException;
}
