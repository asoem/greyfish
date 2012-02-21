package org.asoem.greyfish.utils.persistence;

import com.google.common.io.CharStreams;

import java.io.StringReader;

/**
 * User: christoph
 * Date: 21.02.12
 * Time: 18:37
 */
public class Persisters {
    public static <T> T runThroughPersister(Persister persister, Object o, Class<T> clazz) throws Exception {
        final StringBuilder builder = new StringBuilder();
        persister.serialize(o, CharStreams.asWriter(builder));
        return persister.deserialize(new StringReader(builder.toString()), clazz);
    }
}
