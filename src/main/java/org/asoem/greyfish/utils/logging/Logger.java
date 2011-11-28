package org.asoem.greyfish.utils.logging;

/**
 * The {@code Logger} adds vararg methods to the {@link org.slf4j.Logger} API.
 */
public interface Logger extends org.slf4j.Logger {

    void trace(String format, Object ... args);
    void debug(String format, Object ... args);
    void info(String format, Object ... args);
    void warn(String format, Object ... args);
    void error(String format, Object ... args);
}
