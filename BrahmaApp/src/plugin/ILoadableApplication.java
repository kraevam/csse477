package plugin;

import javax.swing.JPanel;

public interface ILoadableApplication {

	public String getId();
	public void start();
	public void stop();
	public void layout(JPanel panel);
}
