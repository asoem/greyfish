package org.asoem.greyfish.cli;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import org.asoem.greyfish.core.io.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static com.google.common.base.Preconditions.*;

/**
 * This {@code ConnectionManager} implementation manages a single connection to a H2 database.
 */
public final class GreyfishH2ConnectionManager implements ConnectionManager, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(GreyfishH2ConnectionManager.class);

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("The H2 database driver could not be found", e);
        }
    }

    private final Supplier<Connection> delegate;
    private String initSql;
    private String finalizeSql;

    private GreyfishH2ConnectionManager(final String url, final String initSql, final String finalizeSql) {
        checkNotNull(url);
        this.initSql = checkNotNull(initSql);
        delegate = Suppliers.memoize(new Supplier<Connection>() {

            @Override
            public Connection get() {
                final String fullURL = String.format(
                        "jdbc:h2:%s;LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0;DB_CLOSE_ON_EXIT=FALSE",
                        url);
                logger.info("Connecting to database at {}", fullURL);

                final Connection newConnection;
                try {
                    newConnection = DriverManager.getConnection(fullURL, "sa", "");
                    logger.info("Connection opened to url {}", fullURL);

                } catch (SQLException e) {
                    throw Throwables.propagate(e);
                }
                assert newConnection != null;

                initDatabase(newConnection);

                return newConnection;
            }

        });
        this.finalizeSql = finalizeSql;
    }

    static GreyfishH2ConnectionManager embedded(final String path) {
        final String absolutePath = path.replace("~", System.getProperty("user.home"));
        final File file = new File(absolutePath + ".h2.db");
        checkState(!file.exists(), "Database file exists: %s", file.getAbsolutePath());

        return create("file:" + path, defaultInitSql(), defaultFinalizeSql());
    }

    static GreyfishH2ConnectionManager inMemory(final String name) {
        checkNotNull(name);
        return create("mem:" + name, defaultInitSql(), defaultFinalizeSql());
    }

    static GreyfishH2ConnectionManager create(final String url, final String initSql, final String finalizeSql) {
        checkNotNull(url);
        return new GreyfishH2ConnectionManager(url, initSql, finalizeSql);
    }

    @Override
    public void releaseConnection(final Connection connection) {
        checkArgument(connection == delegate.get(),
                "This connection was not created by this supplier");
    }

    @Override
    public Connection get() {
        return delegate.get();
    }

    @Override
    public void close() throws IOException {
        // TODO: Verify that the connection has been opened!
        try {
            try (Statement statement = get().createStatement()) {
                statement.execute(finalizeSql);
            }
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        } finally {
            try {
                get().close();
            } catch (SQLException e) {
                throw new IOException(e);
            }
        }
    }

    private void initDatabase(final Connection newConnection) {
        try {
            try (Statement statement = newConnection.createStatement()) {
                statement.execute(this.initSql);
            }
        } catch (Throwable e) {
            throw Throwables.propagate(e);
        }
    }

    public static String defaultInitSql() {
        final URL resource = GreyfishH2ConnectionManager.class.getResource("/h2/DefaultExperimentDatabase.init.sql");
        try {
            return Resources.asCharSource(resource, Charsets.UTF_8).read();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static String defaultFinalizeSql() {
        final URL resource = GreyfishH2ConnectionManager.class.getResource("/h2/DefaultExperimentDatabase.finalize.sql");
        try {
            return Resources.asCharSource(resource, Charsets.UTF_8).read();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
