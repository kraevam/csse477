package plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;


public class PluginManager implements Runnable {
	private PluginCore core;
	private WatchDir watchDir;
	private HashMap<Path, Plugin> pathToPlugin;

	public PluginManager(PluginCore core) throws IOException {
		this.core = core;
		this.pathToPlugin = new HashMap<Path, Plugin>();
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
	
	void processBundles() {
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

	void loadBundle(Path bundlePath) throws Exception {
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
        
        // Create a new instance of the plugin class and add to the core
        Plugin plugin = (Plugin)pluginClass.newInstance();
        this.core.addPlugin(plugin);
        this.pathToPlugin.put(bundlePath, plugin);

        // Release the jar resources
        jarFile.close();
	}
	
	void unloadBundle(Path bundlePath) {
		Plugin plugin = this.pathToPlugin.remove(bundlePath);
		if(plugin != null) {
			this.core.removePlugin(plugin.getId());
		}
	}
}
