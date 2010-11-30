package joe.util.bootstrap;

/**
 * Marker interface indicating that the entry point to the application is a {@code bootstrapMain} method in this class.
 * The method is {@code public static void} and may declare any thrown exception.
 * 
 * @author Joe Kearney
 */
public interface BootstrappedEntryPoint {
	/** name of the entry point method */
	public static final String BOOTSTRAP_MAIN_METHOD_NAME = "bootstrapMain";
}
