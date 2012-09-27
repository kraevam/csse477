package extension;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public enum ExtensionsManager {

	INSTANCE;
	
	private Set<IBrahmaExtension> allExtensions;
	private Set<IPluginListenerExtension> pluginListenerExtensions;
	private Set<IStatusExtension> statusExtensions;

	private ExtensionsManager() {
		allExtensions = new HashSet<IBrahmaExtension>();
		pluginListenerExtensions = new HashSet<>();
		statusExtensions = new HashSet<>();
	}

	/**
	 * Registers an extension into the Brahma application so that it can receive any information it would need
	 * @param jarFile The jar file that is supposed to contain an extension
	 * @return True if the jar file is a valid extension to the Brahma Application
	 */
	public boolean registerExtension(JarFile jarFile) {
		Manifest manifest = null;
		try {
			manifest = jarFile.getManifest();
		} catch (IOException e) {
			// TODO: log messages for better testability
			e.printStackTrace();
		}
		if (manifest == null) {
			return false; // cannot load
		}

		String className = manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
		if (className == null) {
			return false; // cannot load
		}

		boolean result = true;
		
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		try {
			Class<?> extensionClass = classLoader.loadClass(className);

			if (extensionClass.isAssignableFrom(IBrahmaExtension.class)) {
				IBrahmaExtension extension = (IBrahmaExtension)extensionClass.newInstance();
				allExtensions.add(extension);
				
				// add this extension instance to any appropriate category
				if (extensionClass.isAssignableFrom(IPluginListenerExtension.class)) {
					IPluginListenerExtension pluginListenerExtension = (IPluginListenerExtension)extension;
					pluginListenerExtensions.add(pluginListenerExtension);
				}
				if (extensionClass.isAssignableFrom(IStatusExtension.class)) {
					IStatusExtension statusExtension = (IStatusExtension)extensionClass.newInstance();
					statusExtensions.add(statusExtension);
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			// TODO Make a pretty log for better testability
			e.printStackTrace();
			result = false;
		}
		
		return result;
	}

	public Set<IBrahmaExtension> getAllExtensions() {
		return Collections.unmodifiableSet(allExtensions);
	}
	
	public Set<IPluginListenerExtension> getPluginListenerExtensions() {
		return Collections.unmodifiableSet(pluginListenerExtensions);
	}
	
	public Set<IStatusExtension> getStatusExtensions() {
		return Collections.unmodifiableSet(statusExtensions);
	}
}

