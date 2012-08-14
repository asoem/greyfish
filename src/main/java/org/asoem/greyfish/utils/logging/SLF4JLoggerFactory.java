package org.asoem.greyfish.utils.logging;

import org.slf4j.LoggerFactory;

/**
 * This {@code SLF4JLoggerFactory} clones the static methods found in {@link org.slf4j.LoggerFactory},
 * and decorates the created {@link org.slf4j.Logger} instances with a {@link SLF4JLoggerAdaptor}.
 */
public class SLF4JLoggerFactory {

    public static SLF4JLogger getLogger(Class<?> clazz) {
        return new SLF4JLoggerAdaptor(LoggerFactory.getLogger(clazz));
    }

    public static SLF4JLogger getLogger(String name) {
        return new SLF4JLoggerAdaptor(LoggerFactory.getLogger(name));
    }
}
