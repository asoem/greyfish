package org.asoem.greyfish.utils.logging;

/**
 * This {@code LoggerFactory} clones the static methods found in {@link org.slf4j.LoggerFactory},
 * and decorates the created {@link org.slf4j.Logger} instances with a {@link SLF4JLoggerAdaptor}.
 */
public class LoggerFactory {

    public static Logger getLogger(Class<?> clazz) {
        return new SLF4JLoggerAdaptor(org.slf4j.LoggerFactory.getLogger(clazz));
    }

    public static Logger getLogger(String name) {
        return new SLF4JLoggerAdaptor(org.slf4j.LoggerFactory.getLogger(name));
    }
}
