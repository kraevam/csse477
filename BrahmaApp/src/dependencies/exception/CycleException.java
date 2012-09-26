package dependencies.exception;

@SuppressWarnings("serial")
public class CycleException extends Exception {

	public CycleException(Object source) {
		super("Cycle detected: " + source);
	}
}
