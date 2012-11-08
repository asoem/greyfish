package org.asoem.greyfish.utils.logging;

import org.slf4j.Logger;

/**
 * The {@code SLF4JLogger} adds vararg methods to the {@link org.slf4j.Logger} API.
 */
public interface SLF4JLogger extends Logger {

    void trace(String format, Object ... args);
    void debug(String format, Object ... args);
    void info(String format, Object ... args);
    void warn(String format, Object ... args);
    void error(String format, Object ... args);
}
