package dependencies;

import java.util.jar.JarFile;

public class JarFileTree extends SimpleTree<JarFile> {

	public JarFileTree(JarFile data) {
		super(data);
	}
	
	public JarFileTree(JarFile data, JarFileTree parent) {
		super(data, parent);
	}
	
	@Override
	protected boolean checkEqual(JarFile value1, JarFile value2) {
		if(value1.equals(value2))
			return true;
		
		System.out.println(value1.getName() + "; " + value2.getName());
		return value1.getName().equals(value2.getName());
	}

}
