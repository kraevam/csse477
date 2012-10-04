package plugin;

import java.io.IOException;

import extension.ExtensionsManager;

public class Brahma {
	public static void main(String args[]) {
//		PluginCore core = new PluginCore();
//		core.start();
		ExtensionsManager.INSTANCE.initializeExtensions();
		LoadableApplicationManager pm;
		try {
			pm = new LoadableApplicationManager();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		Thread pmThread = new Thread(pm);
		pmThread.run();
	}
}
