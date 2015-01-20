package org.asoem.greyfish.core.io;

import java.sql.SQLException;

public interface JDBCLogger extends SimulationLogger {
    /**
     * Manually add a logging entry using a custom query.
     *
     * @param query The query to commit.
     */
    void log(StatementProvider query);

    /**
     * Flush this logger. Causes the logger to write all cached (not yet stored stored) logs to the database.
     *
     * @throws java.sql.SQLException
     */
    void flush() throws SQLException;
}
