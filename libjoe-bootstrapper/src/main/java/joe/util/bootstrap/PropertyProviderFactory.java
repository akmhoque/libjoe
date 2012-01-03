package joe.util.bootstrap;


/**
 * Factory that returns a new or existing {@link PropertyProvider} for a {@link BootstrapResult}.
 * The factory may create a new provider, or it may reconfigure an existing one and return that.
 * <p>
 * You can likely get hold of a {@code PropertyProviderFactory} from the type of the {@code PropertyProvider}
 * that you want or from the provider itself.
 * 
 * @author Joe Kearney
 */
public interface PropertyProviderFactory<T extends PropertyProvider> {
	/**
	 * Gets a {@link PropertyProvider} for the specified {@link BootstrapResult}.
	 * 
	 * @param bootstrapResult the {@code BootstrapResult} to wrap
	 * @return the {@link PropertyProvider}
	 */
	T providerFor(BootstrapResult bootstrapResult);
}
