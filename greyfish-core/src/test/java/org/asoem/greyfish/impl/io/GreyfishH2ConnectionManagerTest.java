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

package org.asoem.greyfish.impl.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.sql.Connection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


public class GreyfishH2ConnectionManagerTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testInMemory() throws Exception {
        // given
        final GreyfishH2ConnectionManager manager = GreyfishH2ConnectionManager.inMemory("");

        // when
        final Connection connection = manager.get();
        final boolean connectionValid;
        try {
            connectionValid = connection.isValid(100);
        } finally {
            connection.close();
        }

        // then
        assertThat(connectionValid, is(true));
    }

    @Test
    public void testEmbedded() throws Exception {
        // given
        final File file = temporaryFolder.newFile();
        final GreyfishH2ConnectionManager manager = GreyfishH2ConnectionManager.embedded(file.getAbsolutePath());

        // when
        final Connection connection = manager.get();
        final boolean connectionValid;
        try {
            connectionValid = connection.isValid(100);
        } finally {
            connection.close();
        }

        // then
        assertThat(connectionValid, is(true));
    }
}
