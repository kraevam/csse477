package dependencies;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import dependencies.exception.CycleException;
import dependencies.exception.CycleDependencyException;

public class DependencyResolver {

	private JarFile jarFile;

	public DependencyResolver(JarFile jarFile) {
		this.jarFile = jarFile;
	}

	public List<JarFile> getOrderedDependencies() throws CycleDependencyException {
		List<JarFile> result = new ArrayList<JarFile>();
		Set<String> included = new HashSet<String>();
		SimpleTree<JarFile> dependenciesTree = getDependenciesTree();
		Iterator<JarFile> iterator = dependenciesTree.getChildrenFirstIterator();
		while(iterator.hasNext()) {
			JarFile next = iterator.next();
			if (!included.contains(next.getName())) {
				included.add(next.getName());
				result.add(next);
			}
		}
		return result;
	}
	
	public SimpleTree<JarFile> getDependenciesTree() throws CycleDependencyException {
		JarFileTree dependencyRoot = new JarFileTree(jarFile);
		buildDependenciesTreeForRootJar(dependencyRoot);
		return dependencyRoot;
	}

	private void buildDependenciesTreeForRootJar(JarFileTree dependencyRoot) throws CycleDependencyException {
		JarFile rootJar = dependencyRoot.getData();
		List<JarFile> jarDependencies = getDependenciesForJar(rootJar);

		for (JarFile dependencyJar : jarDependencies) {
			JarFileTree dependency = new JarFileTree(dependencyJar, dependencyRoot);
			try {
				dependencyRoot.addChild(dependency);
			} catch (CycleException e) {
				// This dependency causes a cycle!
				throw new CycleDependencyException(dependencyJar, jarFile);
			}
			buildDependenciesTreeForRootJar(dependency);
		}
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
		if (dependencies == null) {
			// no dependencies - return the empty list
			return dependenciesList;
		}
		String currentFilepath = jarFile.getName();
		Path currentPath = new File(currentFilepath).toPath();
		StringTokenizer st = new StringTokenizer(dependencies, " ");
		while(st.hasMoreTokens()) {
			String dependency = st.nextToken();
			Path resolvedPath = currentPath.resolveSibling(dependency);
			JarFile dependencyJar = null;
			try {
				dependencyJar = new JarFile(resolvedPath.toFile());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dependenciesList.add(dependencyJar);
		}

		return dependenciesList;
	}
	
	
}
