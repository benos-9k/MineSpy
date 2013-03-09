package minespy;

public class ConfigLoadException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ConfigLoadException() {
		
	}

	public ConfigLoadException(String arg0) {
		super(arg0);
	}

	public ConfigLoadException(Throwable arg0) {
		super(arg0);
	}

	public ConfigLoadException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
