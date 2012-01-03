package joe.util.bootstrap;

import static com.google.common.collect.Lists.newLinkedList;

import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Handles logging from {@link BootstrapMain}.
 * @author Joe Kearney
 */
final class BootstrapLogger {
	private final Queue<String> logQueue = newLinkedList();
	private final Map<String, String> applicationProperties;

	BootstrapLogger(Map<String, String> applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	/**
	 * Logs the message directly or stashes in a queue until {@link #flushLogQueue()} is called.
	 * 
	 * @param message log message
	 */
	void log(String message) {
		if (!isLoggingDisabled()) {
			if (isLoggingEnabled()) {
				// actually log
				flushLogQueue();
				doLog(message);
			} else {
				// we're in queue mode and can't flush anything
				logQueue.add(message);
			}
		}
	}

	/**
	 * Actually log a message. This will use either a {@link java.util.logging.Logger}, or {@link System#out}.
	 * 
	 * @param logMessage message to be logged
	 * @see BootstrapMain#BOOTSTRAP_ENABLE_JAVA_UTIL_LOGGING_KEY
	 */
	private void doLog(String message) {
		if (isJulLoggingEnabled()) {
			getJulLogger().info(message);
		} else {
			System.out.println(formatLogMessageForSysout(message));
		}
	}

	private Logger julLogger;
	private Logger getJulLogger() {
		if (julLogger == null) {
			LogManager.getLogManager().reset();
			return (julLogger = Logger.getLogger(BootstrapLogger.class.getName()));
		} else {
			return julLogger;
		}
	}

	/**
	 * Writes out all queued log messages if logging has been explicitly enabled, else clears the queue, permanently
	 * discarding the messages.
	 */
	void flushLogQueue() {
		if (isLoggingEnabled()) {
			while (!logQueue.isEmpty()) {
				doLog(logQueue.poll());
			}
		} else {
			logQueue.clear();
		}
	}

	/**
	 * Has logging been explicitly disabled?
	 * 
	 * @return {@code true} iff the property {@link BootstrapMain#BOOTSTRAP_ENABLE_LOGGING_KEY} has been set to
	 *         {@code false}
	 */
	boolean isLoggingDisabled() {
		return "false".equalsIgnoreCase(applicationProperties.get(BootstrapMain.BOOTSTRAP_ENABLE_LOGGING_KEY));
	}

	/**
	 * Has logging been explicitly enabled?
	 * 
	 * @return {@code true} iff the property {@link BootstrapMain#BOOTSTRAP_ENABLE_LOGGING_KEY} has been set to
	 *         {@code true}
	 */
	boolean isLoggingEnabled() {
		return "true".equalsIgnoreCase(applicationProperties.get(BootstrapMain.BOOTSTRAP_ENABLE_LOGGING_KEY));
	}
	/**
	 * Should logging be done through {@code java.util.logging}, rather than {@code System.out}?
	 * 
	 * @return {@code true} iff the property {@link BootstrapMain#BOOTSTRAP_ENABLE_JAVA_UTIL_LOGGING_KEY} has been set
	 *         to {@code true}
	 */
	boolean isJulLoggingEnabled() {
		return "true".equalsIgnoreCase(applicationProperties.get(BootstrapMain.BOOTSTRAP_ENABLE_JAVA_UTIL_LOGGING_KEY));
	}
	
	/**
	 * Enriches the log message with the datetime and current thread name.
	 * 
	 * @param message raw log message
	 * @return enriched message
	 */
	private static String formatLogMessageForSysout(String message) {
		return String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL [%2$s] - %3$s",
				new Date(), Thread.currentThread().getName(), message);
	}
}
