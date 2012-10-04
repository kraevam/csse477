package dependencies;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

import org.junit.Test;

import dependencies.exception.CycleDependencyException;


public class DependencyResolverTest {

	private static final JarFile test1 = initializeTest1();
	private static final JarFile test2 = initializeTest2();
	private static final JarFile test3 = initializeTest3();
	
	private static JarFile initializeTest1() {
		JarFile result = null; 
		try {
			result = new JarFile("resources/BouncingBall_test.jar");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	private static JarFile initializeTest3() {
		JarFile result = null; 
		try {
			result = new JarFile("resources/BouncingBall_NonCyclicDependency.jar");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	private static JarFile initializeTest2() {
		JarFile result = null; 
		try {
			result = new JarFile("resources/BouncingBall_noDependencies.jar");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	@Test
	public void testGetOrderedDependencies() {
		DependencyResolver dependencyResolver = new DependencyResolver(test1);
		DependencyResolver resolver2 = new DependencyResolver(test2);
		DependencyResolver resolver3 = new DependencyResolver(test3);
		
		try {
			List<JarFile> dependencies = dependencyResolver.getOrderedDependencies();
			fail("Exception was not thrown");
		} catch (Exception e) {
			assertEquals(e.getClass(), CycleDependencyException.class);
		}
		
		try {
			List<JarFile> dependencies = resolver2.getOrderedDependencies();
			assertEquals(1, dependencies.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Thrown exception!");
		}
		

		try {
			List<JarFile> dependencies = resolver3.getOrderedDependencies();
			assertEquals(3, dependencies.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Thrown exception!");
		}
	}

}
