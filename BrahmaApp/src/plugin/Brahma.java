package plugin;

import java.io.IOException;

public class Brahma {
	public static void main(String args[]) {
//		PluginCore core = new PluginCore();
//		core.start();
		PluginManager pm;
		try {
			pm = new PluginManager();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		Thread pmThread = new Thread(pm);
		pmThread.run();
	}
}
