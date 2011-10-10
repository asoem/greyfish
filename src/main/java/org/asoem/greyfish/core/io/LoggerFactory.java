package org.asoem.greyfish.core.io;

/**
 * User: christoph
 * Date: 28.04.11
 * Time: 09:41
 */
public class LoggerFactory {

    public static Logger getLogger(Class<?> clazz) {
        return new SLF4JLoggerAdaptor(org.slf4j.LoggerFactory.getLogger(clazz));
    }

    public static Logger getLogger(String name) {
        return new SLF4JLoggerAdaptor(org.slf4j.LoggerFactory.getLogger(name));
    }
}
