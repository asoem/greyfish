package org.asoem.greyfish.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarFile;

public final class Resources {

    private Resources() {
    }

    /**
     * Get the jar file containing the given {@code clazz}. <p>If the class is not contained inside a jar file an {@link
     * java.lang.UnsupportedOperationException} is thrown.</p>
     *
     * @param clazz the class which is assumed to be contained in a jar file
     * @return the jar file for the class
     * @throws IOException if the jar file could not be read
     */
    public static JarFile getJarFile(final Class<?> clazz) throws IOException {
        final String className = clazz.getSimpleName() + ".class";
        final URL resource = clazz.getResource(className);
        final String classPath = resource.toString();
        if (!classPath.startsWith("jar:file:")) {
            // Class not from JAR
            throw new UnsupportedOperationException("class is not in a jar archive");
        }
        final String jarUrlPath = classPath.substring(4, classPath.indexOf("!"));


        try {
            final URL url = new URL(jarUrlPath);
            final URI uri = url.toURI();
            return new JarFile(new File(uri));
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        } catch (MalformedURLException e) {
            throw new AssertionError("Malformed URL: " + jarUrlPath, e);
        }
    }
}
