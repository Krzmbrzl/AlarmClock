package util;

public class NTPException extends Exception {
	
	private static final long serialVersionUID = 7678774876671449994L;

	public NTPException() {
	}
	
	public NTPException(String message) {
		super(message);
	}
	
	public NTPException(Throwable cause) {
		super(cause);
	}
	
	public NTPException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NTPException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
