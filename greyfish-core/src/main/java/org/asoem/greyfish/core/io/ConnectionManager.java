package org.asoem.greyfish.core.io;

import com.google.common.base.Supplier;

import java.sql.Connection;

/**
 * A manager for {@link Connection}s used for creating and releasing connections.
 * Clients should always return the connection using {@link #releaseConnection} after usage.
 */
public interface ConnectionManager extends Supplier<Connection> {

    /**
     * Return the connection to this manager.
     * @param connection the connection to return
     */
    void releaseConnection(Connection connection);
}
