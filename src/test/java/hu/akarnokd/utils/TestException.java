package hu.akarnokd.utils;

/**
 * Exception for testing if unchecked expections propagate as-is without confusing with
 * other type of common exceptions.
 */
public final class TestException extends RuntimeException {

    private static final long serialVersionUID = -1438148770465406172L;

    /**
     * Constructs a TestException without message or cause.
     */
    public TestException() {
        super();
    }

    /**
     * Counstructs a TestException with message and cause.
     * @param message the message
     * @param cause the cause
     */
    public TestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a TestException with a message only.
     * @param message the message
     */
    public TestException(String message) {
        super(message);
    }

    /**
     * Constructs a TestException with a cause only.
     * @param cause the cause
     */
    public TestException(Throwable cause) {
        super(cause);
    }
}
