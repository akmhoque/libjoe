package joe.util.bootstrap;

import static com.google.common.collect.Lists.newLinkedList;

import java.util.Date;
import java.util.Map;
import java.util.Queue;

final class BootstrapLogger {
	private final Queue<String> logQueue = newLinkedList();
	private boolean queueLogMessages = true;
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
			final String logMessage = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL [%2$s] - %3$s",
					new Date(), Thread.currentThread().getName(), message);

			if (queueLogMessages && isLoggingEnabled()) {
				// we're in queue mode and must now flush everything
				logQueue.add(logMessage);
				flushLogQueue();
			} else if (queueLogMessages) {
				// we're in queue mode and can't flush anything
				logQueue.add(logMessage);
			} else {
				// actually log
				System.out.println(logMessage);
			}
		}
	}

	/**
	 * Writes out all queued log messages if logging has been explicitly enabled, else clears the queue, permanently
	 * discarding the messages.
	 */
	void flushLogQueue() {
		if (isLoggingEnabled()) {
			queueLogMessages = false;
			while (!logQueue.isEmpty()) {
				System.out.println(logQueue.poll());
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
}
