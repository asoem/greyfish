package org.asoem.greyfish.core.io;

/**
 * User: christoph
 * Date: 28.04.11
 * Time: 09:34
 */
public interface Logger extends org.slf4j.Logger {

    void trace(String format, Object ... args);
    void debug(String format, Object ... args);
    void info(String format, Object ... args);
    void warn(String format, Object ... args);
    void error(String format, Object ... args);
}
