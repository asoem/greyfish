package org.asoem.greyfish.core.io;

import org.apache.log4j.Logger;

public enum GreyfishLogger {
    ROOT_LOGGER(Logger.getLogger("org.asoem.greyfish")),
    CORE_LOGGER(Logger.getLogger("org.asoem.greyfish.core")),
    GFACTIONS_LOGGER(Logger.getLogger("org.asoem.greyfish.core.actions")),
    SIMULATION_LOGGER(Logger.getLogger("org.asoem.greyfish.core.simulation")),
    GUI_LOGGER(Logger.getLogger("org.asoem.greyfish.gui")),
    LOG4J_LOGGER(Logger.getLogger("org.perf4j.TimingLogger"));

    public final Logger logger;

    private GreyfishLogger(final Logger logger) {
        this.logger = logger;
//		PropertyConfigurator.configureAndWatch("log4j.xml", 60*1000);
    }

    public boolean hasTraceEnabled() {
        return logger.isTraceEnabled();
    }

    public void trace(Object message) {
        logger.trace(message);
    }

    public void trace(Object message, Throwable t) {
        logger.trace(message, t);
    }

    public boolean hasInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void info(Object message, Throwable t) {
        logger.info(message, t);
    }

    public void info(Object message) {
        logger.info(message);
    }

    public boolean hasDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(Object message) {
        logger.debug(message);
    }

    public void debug(Object message, Throwable t) {
        logger.debug(message, t);
    }

    public void error(Object message) {
        logger.error(message);
    }

    public void error(Object message, Throwable t) {
        logger.error(message, t);
    }

    public void fatal(Object message) {
        logger.fatal(message);
    }

    public void fatal(Object message, Throwable t) {
        logger.fatal(message, t);
    }

    public void warn(Object message, Throwable t) {
        logger.warn(message, t);
    }

    public void warn(Object message) {
        logger.warn(message);
    }
}
