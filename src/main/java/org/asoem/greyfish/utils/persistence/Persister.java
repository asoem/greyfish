package org.asoem.greyfish.utils.persistence;

import java.io.File;
import java.io.Reader;
import java.io.Writer;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:11
 */
public interface Persister {
    public <T> T deserialize(File file, Class<T> clazz) throws Exception;
    public <T> T deserialize(Reader reader, Class<T> clazz) throws Exception;
    public void serialize(Object object, File file) throws Exception;
    public void serialize(Object object, Writer writer) throws Exception;
}
