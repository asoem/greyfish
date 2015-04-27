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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public interface Persister {
    public <T> T deserialize(File file, Class<T> clazz) throws IOException, ClassCastException, ClassNotFoundException;

    public <T> T deserialize(InputStream inputStream, Class<T> clazz) throws IOException, ClassCastException, ClassNotFoundException;

    public void serialize(Object object, File file) throws IOException;

    public void serialize(Object object, OutputStream outputStream) throws IOException;
}
