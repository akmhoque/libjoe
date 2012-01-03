package joe.util.bootstrap;

/**
 * Wrapper for exceptions thrown by the target application.
 * 
 * @author Joe Kearney
 */
public class BootstrapException extends RuntimeException {
	private static final long serialVersionUID = 8146665996333754838L;

	public BootstrapException(String message, Throwable cause) {
		super(message, cause);
	}
	public BootstrapException(Throwable cause) {
		super(cause);
	}
}
