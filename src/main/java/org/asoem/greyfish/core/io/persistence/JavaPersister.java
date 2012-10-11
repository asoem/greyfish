package org.asoem.greyfish.core.io.persistence;

import org.asoem.greyfish.utils.persistence.Persister;

import java.io.*;

/**
 * User: christoph
 * Date: 10.10.12
 * Time: 19:45
 */
public class JavaPersister implements Persister {
    @Override
    public <T> T deserialize(File file, Class<T> clazz) throws PersistenceException, FileNotFoundException {
        return deserialize(new BufferedInputStream(new FileInputStream(file)), clazz);
    }

    @Override
    public <T> T deserialize(InputStream inputStream, Class<T> clazz) throws PersistenceException {
        if (inputStream == null) {
            throw new IllegalArgumentException("The InputStream must not be null");
        }
        ObjectInputStream in = null;
        try {
            // stream closed in the finally
            in = new ObjectInputStream(inputStream);
            return clazz.cast(in.readObject());

        } catch (ClassNotFoundException ex) {
            throw new PersistenceException(ex);
        } catch (IOException ex) {
            throw new PersistenceException(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    @Override
    public void serialize(Object object, File file) throws PersistenceException, FileNotFoundException {
        serialize(object, new BufferedOutputStream(new FileOutputStream(file)));
    }

    @Override
    public void serialize(Object object, OutputStream outputStream) throws PersistenceException {
        if (outputStream == null) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        }
        ObjectOutputStream out = null;
        try {
            // stream closed in the finally
            out = new ObjectOutputStream(outputStream);
            out.writeObject(object);

        } catch (IOException ex) {
            throw new PersistenceException(ex);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }
}
