package dependencies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class DependencyResolver {

	public SimpleTree<JarFile> getDependenciesTree(JarFile jarFile) /*throws CyclicDependencyException*/ {
		SimpleTree<JarFile> dependencyTree = new SimpleTree<JarFile>(jarFile);
		List<JarFile> jarDependencies = getDependenciesForJar(jarFile);
		while (!jarDependencies.isEmpty()) {
			for (JarFile dependency : jarDependencies) {
				SimpleTree<JarFile> dependencyJar = getDependenciesTree(dependency);
				dependencyTree.addChild(dependencyJar);
			}
		}
		
		return dependencyTree;
	}
	
	private List<JarFile> getDependenciesForJar(JarFile jarFile) {
		List<JarFile> dependenciesList = new ArrayList<JarFile>();
		Manifest manifest = null;
		try {
			manifest = jarFile.getManifest();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		String dependencies = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
		StringTokenizer st = new StringTokenizer(dependencies, " ");
		while(st.hasMoreTokens()) {
			String dependency = st.nextToken();
			JarFile dependencyJar = null;
			try {
				dependencyJar = new JarFile(dependency);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dependenciesList.add(dependencyJar);
		}
		
		return dependenciesList;
	}
}
