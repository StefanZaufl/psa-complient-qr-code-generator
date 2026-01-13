package at.klickmagiesoftware.epcqr.exception;

/**
 * Exception thrown when QR code generation fails.
 *
 * <p>This typically wraps underlying QR code library exceptions (e.g., from ZXing)
 * and indicates a failure in the actual QR code encoding process.
 */
public final class GenerationException extends EpcQrCodeException {

    /**
     * Creates a new generation exception with the specified message.
     *
     * @param message the detail message
     */
    public GenerationException(String message) {
        super(message);
    }

    /**
     * Creates a new generation exception with the specified message and cause.
     *
     * @param message the detail message
     * @param cause the underlying cause (typically a ZXing exception)
     */
    public GenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
