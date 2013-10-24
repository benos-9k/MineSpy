package minespy;

public class AbnormalTerminationException extends Exception {

	private static final long serialVersionUID = 1L;

	public AbnormalTerminationException() {
		
	}

	public AbnormalTerminationException(String arg0) {
		super(arg0);
	}

	public AbnormalTerminationException(Throwable arg0) {
		super(arg0);
	}

	public AbnormalTerminationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
