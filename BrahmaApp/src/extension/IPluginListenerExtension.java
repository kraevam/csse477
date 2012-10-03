package extension;

import plugin.ILoadableApplication;

/**
 * An interface for all extensions that want to be notified when a plugin has been loaded
 * @author kraevam
 *
 */
public interface IPluginListenerExtension extends IBrahmaExtension {

	public void pluginLoaded(ILoadableApplication plugin);
	public void pluginUnloaded(ILoadableApplication plugin);
}
