package plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import extension.ExtensionsManager;
import extension.IPluginListenerExtension;
import extension.IStatusExtension;
import fileSystemListener.DirectoryAction;
import fileSystemListener.WatchDir;


public class PluginManager implements Runnable {
	private WatchDir watchDir;
	private HashMap<Path, IPlugin> pathToPlugin;
	
	public PluginManager() throws IOException {
		this.pathToPlugin = new HashMap<Path, IPlugin>();
		watchDir = new WatchDir(FileSystems.getDefault().getPath("plugins"), false);
	}

	@Override
	public void run() {
		// First load existing plugins if any
		try {
			Path pluginDir = FileSystems.getDefault().getPath("plugins");
			File pluginFolder = pluginDir.toFile();
			File[] files = pluginFolder.listFiles();
			if(files != null) {
				for(File f : files) {
					this.loadBundle(f.toPath());
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		// Listen for newly added plugins
		processBundles();
	}
	
	private void processBundles() {
		Map<DirectoryAction, Path> loadMap = watchDir.processEvent();
		Path child;
		
		while (!loadMap.containsKey(DirectoryAction.END)) {
			if (loadMap.isEmpty())
				continue;

			try {
				child = loadMap.get(DirectoryAction.LOAD);
				if (child != null) {
					loadBundle(child);
				}

				child = loadMap.get(DirectoryAction.UNLOAD);
				if (child != null) {
					unloadBundle(child);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void loadBundle(Path bundlePath) throws Exception {
		// Get hold of the jar file
		File jarBundle = bundlePath.toFile();
		JarFile jarFile = new JarFile(jarBundle);
		
		// Get the manifest file in the jar file
		Manifest mf = jarFile.getManifest();
        Attributes mainAttribs = mf.getMainAttributes();
        
        // Get hold of the Plugin-Class attribute and load the class
        String className = mainAttribs.getValue("Plugin-Class");
        URL[] urls = new URL[]{bundlePath.toUri().toURL()};
        ClassLoader classLoader = new URLClassLoader(urls);
        Class<?> pluginClass = classLoader.loadClass(className);
        
        // Create a new instance of the plugin
        if (pluginClass.isAssignableFrom(IPlugin.class)) {
        	IPlugin plugin = (IPlugin)pluginClass.newInstance();
        	this.pathToPlugin.put(bundlePath, plugin);	
        	notifyLoaded(plugin);
    		notifyStatus("Plugin loaded: " + bundlePath.toString());
        }
        
        // Release the jar resources
        jarFile.close();
        
        // check if this is an extension to our app and if so, register it
        ExtensionsManager.INSTANCE.registerExtension(jarFile);
	}
	
	private void unloadBundle(Path bundlePath) {
		IPlugin plugin = this.pathToPlugin.remove(bundlePath);
		notifyUnloaded(plugin);
		notifyStatus("Plugin unloaded: " + bundlePath.toString());
	}
	
	private void notifyLoaded(IPlugin plugin) {
		for (IPluginListenerExtension extension : ExtensionsManager.INSTANCE.getPluginListenerExtensions()) {
			extension.pluginLoaded(plugin);
		}
	}
	
	private void notifyUnloaded(IPlugin plugin) {
		for (IPluginListenerExtension extension : ExtensionsManager.INSTANCE.getPluginListenerExtensions()) {
			extension.pluginUnloaded(plugin);
		}
	}
	
	// TODO: call this whenever a plugin is loaded/unloaded and whenever there is a dependency problem of some sort
	private void notifyStatus(String message) {
		for (IStatusExtension extension : ExtensionsManager.INSTANCE.getStatusExtensions()) {
			extension.addToStatus(message);
		}
	}
}
