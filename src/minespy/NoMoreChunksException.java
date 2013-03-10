package minespy;

public class NoMoreChunksException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NoMoreChunksException() {

	}

	public NoMoreChunksException(String arg0) {
		super(arg0);
	}

	public NoMoreChunksException(Throwable arg0) {
		super(arg0);
	}

	public NoMoreChunksException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
