package joe.util.bootstrap;

import static com.google.common.collect.Lists.*;

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

	void log(String string) {
		if (!isLoggingDisabled()) {
			final String logMessage = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL [%2$s] - %3$s",
					new Date(), Thread.currentThread().getName(), string);
	
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

	boolean isLoggingDisabled() {
		return "false".equalsIgnoreCase(applicationProperties.get(BootstrapMain.BOOTSTRAP_PROPERTY_LOGGING_KEY));
	}

	private boolean isLoggingEnabled() {
		return "true".equalsIgnoreCase(applicationProperties.get(BootstrapMain.BOOTSTRAP_PROPERTY_LOGGING_KEY));
	}
}