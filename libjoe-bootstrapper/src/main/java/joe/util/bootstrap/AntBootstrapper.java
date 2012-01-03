package joe.util.bootstrap;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Predicates.containsPattern;
import static com.google.common.collect.Maps.filterKeys;
import static joe.util.PropertyUtils.getStringEntries;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant task to load bootstrapped properties into the project.
 * 
 * @author Joe Kearney
 */
public final class AntBootstrapper extends Task {
	@Override
	public void execute() throws BuildException {
		Map<String, String> bootstrapProps = filterKeys(getStringEntries(getProject().getProperties()),
				containsPattern("bootstrap"));

		if (requirePresetEnvironment) {
			String presetEnvironment = bootstrapProps.get(BootstrapMain.BOOTSTRAP_ENVIRONMENT_KEY);
			checkState(presetEnvironment != null, "The " + BootstrapMain.BOOTSTRAP_ENVIRONMENT_KEY
					+ " key must be set if requireEnvironmentPreset is set to true");
			log("Bootstrapping environment " + presetEnvironment + "...");
		} else {
			log("Bootstrapping...");
		}

		System.getProperties().putAll(bootstrapProps);
		System.out.println("bootstrap.properties.root.dir=" + bootstrapProps.get("bootstrap.properties.root.dir"));
		BootstrapResult bootstrapResult = BootstrapMain.prepareProperties();
		Map<String, String> publishedSystemProperties = bootstrapResult.getPublishedSystemProperties();
		log("Loading bootstrapped properties into project properties");
		for (Entry<String, String> entry : publishedSystemProperties.entrySet()) {
			getProject().setProperty(entry.getKey(), entry.getValue());
		}
		log("Bootstrapped properties loaded for environment "
				+ publishedSystemProperties.get(BootstrapMain.BOOTSTRAP_ENVIRONMENT_KEY));
	}

	private boolean requirePresetEnvironment = false;

	/**
	 * Require that the {@link BootstrapMain#BOOTSTRAP_ENVIRONMENT_KEY} property has been set in the project properties
	 * before bootstrapping is begun.
	 * 
	 * @param requirePresetEnvironment {@code true} to require this
	 */
	public void setRequirePresetEnvironment(boolean requirePresetEnvironment) {
		this.requirePresetEnvironment = requirePresetEnvironment;
	}
}
