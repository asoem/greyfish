package org.asoem.greyfish.cli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.sql.Connection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: christoph Date: 06.09.13 Time: 11:19
 */
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
