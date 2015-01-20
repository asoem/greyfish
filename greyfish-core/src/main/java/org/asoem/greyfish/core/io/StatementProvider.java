package org.asoem.greyfish.core.io;

import com.google.common.base.Function;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementProvider {

    /**
     * Set the values for the prepared {@code statement}.
     *
     * @param statementFactory the statement factory
     * @throws java.sql.SQLException if an error occurred when setting values
     */
    PreparedStatement prepareStatement(Function<? super String, ? extends PreparedStatement> statementFactory)
            throws SQLException;
}
