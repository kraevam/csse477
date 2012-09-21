package extension;

import plugin.IPlugin;

/**
 * An interface for all extensions that want to be notified when a plugin has been loaded
 * @author kraevam
 *
 */
public interface IPluginLoadListener extends IBrahmaExtension {

	public void pluginLoaded(IPlugin plugin);
	public void pluginUnloaded(IPlugin plugin);
}
