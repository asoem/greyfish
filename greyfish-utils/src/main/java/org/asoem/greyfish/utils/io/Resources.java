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

package org.asoem.greyfish.utils.io;

import org.asoem.greyfish.utils.base.ClassNotInstantiableError;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarFile;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Resources {

    private Resources() {
        throw new ClassNotInstantiableError();
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
        checkNotNull(clazz);
        final String className = clazz.getName().substring(clazz.getName().lastIndexOf('.') + 1) + ".class";
        @Nullable
        final URL resource = clazz.getResource(className);
        if (resource == null) {
            throw new IOException("No such resource " + className);
        }

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
