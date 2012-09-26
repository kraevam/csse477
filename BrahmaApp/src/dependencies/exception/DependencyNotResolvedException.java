package dependencies.exception;

@SuppressWarnings("serial")
public class DependencyNotResolvedException extends Exception {

	public DependencyNotResolvedException(String missingJarPath) {
		super("Dependency not found: " + missingJarPath);
	}

}
