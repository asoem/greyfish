package org.asoem.greyfish.core.io;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Enumeration;
import java.util.ResourceBundle;

public enum GreyfishLogger {
	ROOT_LOGGER(Logger.getLogger("org.asoem.greyfish")),
	LOG4J_LOGGER(Logger.getLogger("org.perf4j.TimingLogger"));
	
	public final Logger logger;

	private GreyfishLogger(final Logger logger) {
        this.logger = logger;
//		PropertyConfigurator.configureAndWatch("log4j.xml", 60*1000);
	}

	public static void addAppender(Appender newAppender) {
		ROOT_LOGGER.logger.addAppender(newAppender);
	}

	public static void assertLog(boolean assertion, String msg) {
		ROOT_LOGGER.logger.assertLog(assertion, msg);
	}

	public static void callAppenders(LoggingEvent arg0) {
		ROOT_LOGGER.logger.callAppenders(arg0);
	}

	public static void debug(Object message, Throwable t) {
		ROOT_LOGGER.logger.debug(message, t);
	}

	public static void debug(Object message) {
		ROOT_LOGGER.logger.debug(message);
	}

	public static void error(Object message, Throwable t) {
		ROOT_LOGGER.logger.error(message, t);
	}

	public static void error(Object message) {
		ROOT_LOGGER.logger.error(message);
	}

    /**
     * This method delegates to {@link org.apache.log4j.Category#fatal(Object, Throwable)}
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     */
	public static void fatal(Object message, Throwable t) {
		ROOT_LOGGER.logger.fatal(message, t);
	}

	public static void fatal(Object message) {
		ROOT_LOGGER.logger.fatal(message);
	}

	public static boolean getAdditivity() {
		return ROOT_LOGGER.logger.getAdditivity();
	}

	public static Enumeration getAllAppenders() {
		return ROOT_LOGGER.logger.getAllAppenders();
	}

	public static Appender getAppender(String name) {
		return ROOT_LOGGER.logger.getAppender(name);
	}

	public static Level getEffectiveLevel() {
		return ROOT_LOGGER.logger.getEffectiveLevel();
	}

	public static Level getLevel() {
		return ROOT_LOGGER.logger.getLevel();
	}

	public static LoggerRepository getLoggerRepository() {
		return ROOT_LOGGER.logger.getLoggerRepository();
	}

	public static String getName() {
		return ROOT_LOGGER.logger.getName();
	}

	public static Category getParent() {
		return ROOT_LOGGER.logger.getParent();
	}

	public static ResourceBundle getResourceBundle() {
		return ROOT_LOGGER.logger.getResourceBundle();
	}

	public static void info(Object message, Throwable t) {
		ROOT_LOGGER.logger.info(message, t);
	}

	public static void info(Object message) {
		ROOT_LOGGER.logger.info(message);
	}

	public static boolean isAttached(Appender appender) {
		return ROOT_LOGGER.logger.isAttached(appender);
	}

	public static boolean isDebugEnabled() {
		return ROOT_LOGGER.logger.isDebugEnabled();
	}

	public static boolean isEnabledFor(Priority level) {
		return ROOT_LOGGER.logger.isEnabledFor(level);
	}

	public static boolean isInfoEnabled() {
		return ROOT_LOGGER.logger.isInfoEnabled();
	}

	public static boolean isTraceEnabled() {
		return ROOT_LOGGER.logger.isTraceEnabled();
	}

	public static void l7dlog(Priority arg0, String arg1, Object[] arg2, Throwable arg3) {
		ROOT_LOGGER.logger.l7dlog(arg0, arg1, arg2, arg3);
	}

	public static void l7dlog(Priority arg0, String arg1, Throwable arg2) {
		ROOT_LOGGER.logger.l7dlog(arg0, arg1, arg2);
	}

	public static void log(Priority priority, Object message, Throwable t) {
		ROOT_LOGGER.logger.log(priority, message, t);
	}

	public static void log(Priority priority, Object message) {
		ROOT_LOGGER.logger.log(priority, message);
	}

	public static void log(String callerFQCN, Priority level, Object message,
			Throwable t) {
		ROOT_LOGGER.logger.log(callerFQCN, level, message, t);
	}

	public static void removeAllAppenders() {
		ROOT_LOGGER.logger.removeAllAppenders();
	}

	public static void removeAppender(Appender appender) {
		ROOT_LOGGER.logger.removeAppender(appender);
	}

	public static void removeAppender(String name) {
		ROOT_LOGGER.logger.removeAppender(name);
	}

	public static void setAdditivity(boolean additive) {
		ROOT_LOGGER.logger.setAdditivity(additive);
	}

	public static void setLevel(Level level) {
		ROOT_LOGGER.logger.setLevel(level);
	}

	public static void setResourceBundle(ResourceBundle bundle) {
		ROOT_LOGGER.logger.setResourceBundle(bundle);
	}

	public static void trace(Object message, Throwable t) {
		ROOT_LOGGER.logger.trace(message, t);
	}

	public static void trace(Object message) {
		ROOT_LOGGER.logger.trace(message);
	}

	public static void warn(Object message, Throwable t) {
		ROOT_LOGGER.logger.warn(message, t);
	}

	public static void warn(Object message) {
		ROOT_LOGGER.logger.warn(message);
	}
}
