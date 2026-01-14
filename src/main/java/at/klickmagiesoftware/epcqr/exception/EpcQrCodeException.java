package at.klickmagiesoftware.epcqr.exception;

/**
 * Base exception for all EPC QR Code library errors.
 *
 * <p>This is an unchecked exception as validation errors typically represent programming errors
 * that should be fixed during development, not handled at runtime.
 */
public class EpcQrCodeException extends RuntimeException {

    /**
     * Creates a new exception with the specified message.
     *
     * @param message the detail message
     */
    public EpcQrCodeException(String message) {
        super(message);
    }

    /**
     * Creates a new exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause
     */
    public EpcQrCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
