package dependencies.exception;

import java.util.jar.JarFile;

@SuppressWarnings("serial")
public class CycleDependencyException extends Exception {

	private JarFile source1;
	private JarFile source2;
	
	public CycleDependencyException(JarFile source1, JarFile source2) {
		super("Cyclic Jar dependency detected!");
		this.source1 = source1;
		this.source2 = source2;
	}

	public JarFile getSource() {
		return source1;
	}
	
	public JarFile getSource2() {
		return source2;
	}
	
	
}
