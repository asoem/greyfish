package org.asoem.greyfish.core.io;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Enumeration;
import java.util.ResourceBundle;

public enum GreyfishLogger {
	INSTANCE;
	
	public final Logger logger = Logger.getLogger("org.asoem.greyfish");

	private GreyfishLogger() {
//		PropertyConfigurator.configureAndWatch("log4j.xml", 60*1000);
	}

	public static void addAppender(Appender newAppender) {
		INSTANCE.logger.addAppender(newAppender);
	}

	public static void assertLog(boolean assertion, String msg) {
		INSTANCE.logger.assertLog(assertion, msg);
	}

	public static void callAppenders(LoggingEvent arg0) {
		INSTANCE.logger.callAppenders(arg0);
	}

	public static void debug(Object message, Throwable t) {
		INSTANCE.logger.debug(message, t);
	}

	public static void debug(Object message) {
		INSTANCE.logger.debug(message);
	}

	public static void error(Object message, Throwable t) {
		INSTANCE.logger.error(message, t);
	}

	public static void error(Object message) {
		INSTANCE.logger.error(message);
	}

    /**
     * This method delegates to {@link org.apache.log4j.Category#fatal(Object, Throwable)}
     * @param message the message object to log.
     * @param t the exception to log, including its stack trace.
     */
	public static void fatal(Object message, Throwable t) {
		INSTANCE.logger.fatal(message, t);
	}

	public static void fatal(Object message) {
		INSTANCE.logger.fatal(message);
	}

	public static boolean getAdditivity() {
		return INSTANCE.logger.getAdditivity();
	}

	public static Enumeration getAllAppenders() {
		return INSTANCE.logger.getAllAppenders();
	}

	public static Appender getAppender(String name) {
		return INSTANCE.logger.getAppender(name);
	}

	public static Level getEffectiveLevel() {
		return INSTANCE.logger.getEffectiveLevel();
	}

	public static Level getLevel() {
		return INSTANCE.logger.getLevel();
	}

	public static LoggerRepository getLoggerRepository() {
		return INSTANCE.logger.getLoggerRepository();
	}

	public static String getName() {
		return INSTANCE.logger.getName();
	}

	public static Category getParent() {
		return INSTANCE.logger.getParent();
	}

	public static ResourceBundle getResourceBundle() {
		return INSTANCE.logger.getResourceBundle();
	}

	public static void info(Object message, Throwable t) {
		INSTANCE.logger.info(message, t);
	}

	public static void info(Object message) {
		INSTANCE.logger.info(message);
	}

	public static boolean isAttached(Appender appender) {
		return INSTANCE.logger.isAttached(appender);
	}

	public static boolean isDebugEnabled() {
		return INSTANCE.logger.isDebugEnabled();
	}

	public static boolean isEnabledFor(Priority level) {
		return INSTANCE.logger.isEnabledFor(level);
	}

	public static boolean isInfoEnabled() {
		return INSTANCE.logger.isInfoEnabled();
	}

	public static boolean isTraceEnabled() {
		return INSTANCE.logger.isTraceEnabled();
	}

	public static void l7dlog(Priority arg0, String arg1, Object[] arg2, Throwable arg3) {
		INSTANCE.logger.l7dlog(arg0, arg1, arg2, arg3);
	}

	public static void l7dlog(Priority arg0, String arg1, Throwable arg2) {
		INSTANCE.logger.l7dlog(arg0, arg1, arg2);
	}

	public static void log(Priority priority, Object message, Throwable t) {
		INSTANCE.logger.log(priority, message, t);
	}

	public static void log(Priority priority, Object message) {
		INSTANCE.logger.log(priority, message);
	}

	public static void log(String callerFQCN, Priority level, Object message,
			Throwable t) {
		INSTANCE.logger.log(callerFQCN, level, message, t);
	}

	public static void removeAllAppenders() {
		INSTANCE.logger.removeAllAppenders();
	}

	public static void removeAppender(Appender appender) {
		INSTANCE.logger.removeAppender(appender);
	}

	public static void removeAppender(String name) {
		INSTANCE.logger.removeAppender(name);
	}

	public static void setAdditivity(boolean additive) {
		INSTANCE.logger.setAdditivity(additive);
	}

	public static void setLevel(Level level) {
		INSTANCE.logger.setLevel(level);
	}

	public static void setResourceBundle(ResourceBundle bundle) {
		INSTANCE.logger.setResourceBundle(bundle);
	}

	public static void trace(Object message, Throwable t) {
		INSTANCE.logger.trace(message, t);
	}

	public static void trace(Object message) {
		INSTANCE.logger.trace(message);
	}

	public static void warn(Object message, Throwable t) {
		INSTANCE.logger.warn(message, t);
	}

	public static void warn(Object message) {
		INSTANCE.logger.warn(message);
	}
}
