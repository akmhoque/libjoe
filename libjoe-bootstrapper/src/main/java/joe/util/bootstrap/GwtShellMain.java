package joe.util.bootstrap;

/**
 * An entry point whose sole purpose is to launch a GWT shell application through the bootstrapper. The only change you
 * need make is to set the main class in the launch script to be this class, and to set the property
 * {@code bootstrap.enable=true} as a system property or in one of the property files read by the bootstrapper on
 * launch.
 * 
 * @author Joe Kearney
 * @see BootstrapMain
 * @see BootstrapMain#BOOTSTRAP_ENABLE_KEY
 */
public final class GwtShellMain {
	private static final String GWT_SHELL_MAIN_CLASS_NAME = "com.google.gwt.dev.GWTShell";

	public static void main(String[] args) throws ClassNotFoundException {
		BootstrapMain.withMainArgs(args).launchApplication(GWT_SHELL_MAIN_CLASS_NAME);
	}
}
