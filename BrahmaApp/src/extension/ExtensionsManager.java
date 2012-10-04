package extension;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import dependencies.DependencyResolver;
import dependencies.exception.CycleDependencyException;

import plugin.BrahmaGlobalProperties;

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

	public void initializeExtensions() {
		final String extensionsPath = 
				BrahmaGlobalProperties.INSTANCE.getProperty(BrahmaGlobalProperties.EXTENSIONS_DIR_PROPERTY);
		File extensionsDirFile = new File(extensionsPath);
		if (!extensionsDirFile.exists() || !extensionsDirFile.isDirectory()) {
			System.out.println("Please check configurations. File does not exist or is not a directory:\n" + BrahmaGlobalProperties.EXTENSIONS_DIR_PROPERTY);
			// run BrahmaApp without extensions
			return;
		}
		
		for(File extensionFile : extensionsDirFile.listFiles()) {
			JarFile extensionJar = null;
			try {
				extensionJar = new JarFile(extensionFile);
			} catch (IOException e) {
				// not a jar, move on
				continue;
			}
			
			DependencyResolver dependencyResolver = new DependencyResolver(extensionJar);
			List<JarFile> dependencies;
			try {
				dependencies = dependencyResolver.getOrderedDependencies();
			} catch (CycleDependencyException e1) {
				// Obviously, this jar is problematic; Don't load it and move on
				e1.printStackTrace();
				continue;
			}
			
			for (JarFile dependency : dependencies) {
				registerExtension(dependency, (new File(dependency.getName()).toPath()));
			}
			
			if(extensionJar != null) {				
				try {
					extensionJar.close();
				} catch (IOException e) { }
			}
		}
	}
	/**
	 * Registers an extension into the Brahma application so that it can receive any information it would need
	 * @param jarFile The jar file that is supposed to contain an extension
	 * @return True if the jar file is a valid extension to the Brahma Application
	 */
	public boolean registerExtension(JarFile jarFile, Path filePath) {
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

		boolean result = false;
				
		try {
			URL[] urls = new URL[]{filePath.toUri().toURL()};
			URLClassLoader classLoader = new URLClassLoader(urls);
			Class<?> extensionClass = classLoader.loadClass(className);

			if (IBrahmaExtension.class.isAssignableFrom(extensionClass)) {
				IBrahmaExtension extension = (IBrahmaExtension)extensionClass.newInstance();
				allExtensions.add(extension);
				result = true;
				
				// add this extension instance to any appropriate category
				if (IPluginListenerExtension.class.isAssignableFrom(extensionClass)) {
					IPluginListenerExtension pluginListenerExtension = (IPluginListenerExtension)extension;
					pluginListenerExtensions.add(pluginListenerExtension);
				}
				if (IStatusExtension.class.isAssignableFrom(extensionClass)) {
					IStatusExtension statusExtension = (IStatusExtension)extensionClass.newInstance();
					statusExtensions.add(statusExtension);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		
		return result;
	}

	public void unregisterExtension(JarFile jarFile) {
		// TOOD: Unimplemented
		// Maybe implement a map from jar files to all added extensions
		// and in this method remove those extensions from the map and from the corresponding sets that keep them...
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

